package consulo.hub.backend.repository.cleanup;

import consulo.component.util.BuildNumber;
import consulo.hub.backend.repository.RepositoryChannelStore;
import consulo.hub.backend.repository.RepositoryChannelsService;
import consulo.hub.backend.repository.RepositoryNodeState;
import consulo.hub.shared.repository.PluginChannel;
import consulo.hub.shared.repository.PluginNode;
import consulo.hub.shared.repository.util.PlatformNodeDesc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * @author VISTALL
 * @since 2025-02-09
 */
@Service
public class RepositoryCleanupService {
    public static final int ourMaxBuildCount = 25;

    /**
     * valhalla holds experimental snapshot builds (3-SNAPSHOT, 4-SNAPSHOT) with own numbering.
     * It never participates in the release cycle analysis. While disabled, its artifacts are purged on cleanup.
     * The enum value itself can't be removed - store metadata and statistics still reference it.
     */
    public static final boolean VALHALLA_ENABLED = false;

    private static final int ourMaxRemovePerSession = 100;

    private static final Logger LOG = LoggerFactory.getLogger(RepositoryCleanupService.class);

    private final RepositoryChannelsService myRepositoryChannelsService;
    private final TaskExecutor myTaskExecutor;

    public RepositoryCleanupService(RepositoryChannelsService repositoryChannelsService, TaskExecutor taskExecutor) {
        myRepositoryChannelsService = repositoryChannelsService;
        myTaskExecutor = taskExecutor;
    }

    public void runCleanUpAsync() {
        myTaskExecutor.execute(this::runCleanUp);
    }

    public void runCleanUp() {
        for (PluginChannel channel : PluginChannel.values()) {
            if (myRepositoryChannelsService.getRepositoryByChannel(channel).isLoading()) {
                return;
            }
        }

        Set<Path> filesToRemove = new LinkedHashSet<>();

        if (!VALHALLA_ENABLED) {
            purgeValhalla(filesToRemove);
        }

        Map<PlatformNodeDesc, Map<PluginChannel, TreeSet<BuildNumber>>> versions = new LinkedHashMap<>();

        PluginChannel[] pluginChannels = analysisChannels();

        // first of all we collect all versions, groupped by channed
        for (PluginChannel channel : pluginChannels) {
            for (PlatformNodeDesc nodeDesc : PlatformNodeDesc.values()) {
                RepositoryChannelStore channelStore = myRepositoryChannelsService.getRepositoryByChannel(channel);

                RepositoryNodeState pluginsState = channelStore.getState(nodeDesc.id());
                if (pluginsState == null) {
                    continue;
                }

                NavigableMap<String, NavigableSet<PluginNode>> map = pluginsState.getPluginsByPlatformVersion();

                for (Map.Entry<String, NavigableSet<PluginNode>> entry : map.entrySet()) {
                    String platformVersion = entry.getKey();

                    BuildNumber buildNumber = BuildNumber.fromString(platformVersion);
                    if (buildNumber == null) {
                        continue;
                    }

                    versions.computeIfAbsent(nodeDesc, (p) -> new TreeMap<>())
                        .computeIfAbsent(channel, pluginChannel -> new TreeSet<>())
                        .add(buildNumber);
                }
            }
        }

        TreeSet<BuildNumber> allVersions = new TreeSet<>();

        // collect all data
        for (PlatformNodeDesc desc : PlatformNodeDesc.values()) {
            Map<PluginChannel, TreeSet<BuildNumber>> map = versions.getOrDefault(desc, Map.of());

            if (map.size() != pluginChannels.length) {
                versions.remove(desc);
            }
            else {
                for (TreeSet<BuildNumber> set : map.values()) {
                    allVersions.addAll(set);
                }
            }
        }

        Set<BuildNumber> notFullyReleasedBuilds = new HashSet<>();

        for (BuildNumber platformBuildVer : allVersions) {
            for (PlatformNodeDesc desc : PlatformNodeDesc.values()) {
                Map<PluginChannel, TreeSet<BuildNumber>> map = versions.getOrDefault(desc, Map.of());

                boolean[] states = new boolean[pluginChannels.length];
                for (int i = 0; i < pluginChannels.length; i++) {
                    PluginChannel pluginChannel = pluginChannels[i];

                    Set<BuildNumber> buildNumbers = Objects.requireNonNullElse(map.get(pluginChannel), Set.of());

                    states[i] = buildNumbers.contains(platformBuildVer);
                }

                if (!allTrue(states) && !allFalse(states)) {
                    notFullyReleasedBuilds.add(platformBuildVer);
                }
            }
        }

        allVersions.removeAll(notFullyReleasedBuilds);

        List<String> toRemoveBuilds = new ArrayList<>(ourMaxRemovePerSession);
        while (allVersions.size() > ourMaxBuildCount) {
            BuildNumber toRemove = allVersions.removeFirst();

            toRemoveBuilds.add(toRemove.asString());

            if (toRemoveBuilds.size() == ourMaxRemovePerSession) {
                break;
            }
        }

        for (PlatformNodeDesc desc : PlatformNodeDesc.values()) {
            for (PluginChannel channel : pluginChannels) {
                RepositoryChannelStore store = myRepositoryChannelsService.getRepositoryByChannel(channel);

                RepositoryNodeState state = store.getState(desc.id());
                if (state == null) {
                    continue;
                }

                for (String toRemoveBuild : toRemoveBuilds) {
                    PluginNode node = state.select(toRemoveBuild, toRemoveBuild, true);
                    if (node == null) {
                        continue;
                    }

                    addNodeFiles(filesToRemove, node);

                    state.remove(toRemoveBuild, toRemoveBuild);
                }
            }
        }

        Set<String> allPluginIds = new HashSet<>();

        for (PluginChannel channel : pluginChannels) {
            RepositoryChannelStore store = myRepositoryChannelsService.getRepositoryByChannel(channel);

            store.iteratePluginNodes(pluginNode -> allPluginIds.add(pluginNode.id));
        }

        for (PlatformNodeDesc desc : PlatformNodeDesc.values()) {
            allPluginIds.remove(desc.id());
            for (String oldId : desc.oldIds()) {
                allPluginIds.remove(oldId);
            }
        }

        for (PluginChannel channel : pluginChannels) {
            RepositoryChannelStore store = myRepositoryChannelsService.getRepositoryByChannel(channel);

            for (String allPluginId : allPluginIds) {
                RepositoryNodeState state = store.getState(allPluginId);
                if (state == null) {
                    continue;
                }
                
                NavigableMap<String, NavigableSet<PluginNode>> pluginsByPlatformVersion = state.getPluginsByPlatformVersion();

                for (String toRemoveBuild : toRemoveBuilds) {
                    NavigableSet<PluginNode> pluginNodes = pluginsByPlatformVersion.get(toRemoveBuild);
                    if (pluginNodes == null) {
                        continue;
                    }

                    for (PluginNode node : pluginNodes) {
                        addNodeFiles(filesToRemove, node);

                        state.remove(node.version, node.platformVersion);
                    }
                }
            }
        }

        LOG.info("CleanUp: Analyzing repos - marked {} platform builds to remove. All {} files marked to remove with plugins", toRemoveBuilds.size(), filesToRemove.size());

        long startTime = System.currentTimeMillis();
        filesToRemove.parallelStream().forEach(path -> {
            try {
                Files.deleteIfExists(path);

                Path parent = path.getParent();
                if (parent != null) {
                    Path jsonFile = parent.resolve(path.getFileName() + ".json");

                    Files.deleteIfExists(jsonFile);
                }
            } catch (IOException e) {
                LOG.error("Failed to remove " + path, e);
            }
        });
        
        long diff = (System.currentTimeMillis() - startTime) / 1000L;

        LOG.info("CleanUp: Finished to remove files in {} seconds", diff);
    }

    private void purgeValhalla(Set<Path> filesToRemove) {
        RepositoryChannelStore valhallaStore = myRepositoryChannelsService.getRepositoryByChannel(PluginChannel.valhalla);

        List<PluginNode> nodes = new ArrayList<>();
        valhallaStore.iteratePluginNodes(nodes::add);

        if (nodes.isEmpty()) {
            return;
        }

        for (PluginNode node : nodes) {
            boolean shared = false;
            for (PluginChannel channel : PluginChannel.values()) {
                if (channel == PluginChannel.valhalla) {
                    continue;
                }

                if (myRepositoryChannelsService.getRepositoryByChannel(channel).isInRepository(node.id, node.version, node.platformVersion)) {
                    shared = true;
                    break;
                }
            }

            valhallaStore.remove(node.id, node.version, node.platformVersion);

            if (!shared) {
                addNodeFiles(filesToRemove, node);
            }
        }

        LOG.info("CleanUp: purged {} valhalla nodes", nodes.size());
    }

    private static PluginChannel[] analysisChannels() {
        return Arrays.stream(PluginChannel.values())
            .filter(channel -> channel != PluginChannel.valhalla)
            .toArray(PluginChannel[]::new);
    }

    private static void addNodeFiles(Set<Path> filesToRemove, PluginNode node) {
        Path artifactPath = node.targetPath;
        if (artifactPath == null) {
            LOG.warn("CleanUp: no target path for node {}:{}", node.id, node.version);
            return;
        }

        filesToRemove.add(artifactPath);

        // co-located native packages built by PluginPackageStore
        Path pkgDir = artifactPath.getParent();
        if (pkgDir != null) {
            filesToRemove.add(pkgDir.resolve(node.id + "_" + node.version + ".deb"));
            filesToRemove.add(pkgDir.resolve(node.id + "_" + node.version + ".pkg.tar.gz"));
        }
    }

    private static boolean allTrue(boolean[] states) {
        for (boolean state : states) {
            if (!state) {
                return false;
            }
        }
        return true;
    }

    private static boolean allFalse(boolean[] states) {
        for (boolean state : states) {
            if (state) {
                return false;
            }
        }

        return true;
    }
}
