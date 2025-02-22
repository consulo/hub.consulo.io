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

    private static final int ourMaxRemovePerSession = 100;

    private static final Logger logger = LoggerFactory.getLogger(RepositoryCleanupService.class);

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
        Map<PlatformNodeDesc, Map<PluginChannel, TreeSet<BuildNumber>>> versions = new LinkedHashMap<>();

        PluginChannel[] pluginChannels = PluginChannel.values();

        // first of all we collect all versions, groupped by channed
        for (PluginChannel channel : pluginChannels) {
            for (PlatformNodeDesc nodeDesc : PlatformNodeDesc.values()) {
                RepositoryChannelStore channelStore = myRepositoryChannelsService.getRepositoryByChannel(channel);
                if (channelStore.isLoading()) {
                    return;
                }

                RepositoryNodeState pluginsState = channelStore.getState(nodeDesc.id());
                if (pluginsState == null) {
                    continue;
                }

                NavigableMap<String, NavigableSet<PluginNode>> map = pluginsState.getPluginsByPlatformVersion();

                for (Map.Entry<String, NavigableSet<PluginNode>> entry : map.entrySet()) {
                    String platformVersion = entry.getKey();

                    versions.computeIfAbsent(nodeDesc, (p) -> new TreeMap<>())
                        .computeIfAbsent(channel, pluginChannel -> new TreeSet<>())
                        .add(BuildNumber.fromString(platformVersion));
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

        for (PlatformNodeDesc desc : PlatformNodeDesc.values()) {
            Map<PluginChannel, TreeSet<BuildNumber>> map = versions.getOrDefault(desc, Map.of());

            for (Map.Entry<PluginChannel, TreeSet<BuildNumber>> entry : map.entrySet()) {
                TreeSet<BuildNumber> deployedVersions = entry.getValue();

                Iterator<BuildNumber> allVersionsIterator = allVersions.iterator();
                while (allVersionsIterator.hasNext()) {
                    BuildNumber allVersion = allVersionsIterator.next();

                    if (!deployedVersions.contains(allVersion)) {
                        allVersionsIterator.remove();
                    }
                }
            }
        }

        List<String> toRemoveBuilds = new ArrayList<>(ourMaxRemovePerSession);
        while (allVersions.size() > ourMaxBuildCount) {
            BuildNumber toRemove = allVersions.removeFirst();

            toRemoveBuilds.add(toRemove.asString());

            if (toRemoveBuilds.size() == ourMaxRemovePerSession) {
                break;
            }
        }

        Set<Path> filesToRemove = new LinkedHashSet<>();

        for (PlatformNodeDesc desc : PlatformNodeDesc.values()) {
            for (PluginChannel channel : PluginChannel.values()) {
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

                    filesToRemove.add(Objects.requireNonNull(node.targetPath));

                    state.remove(toRemoveBuild, toRemoveBuild);
                }
            }
        }

        Set<String> allPluginIds = new HashSet<>();

        for (PluginChannel channel : PluginChannel.values()) {
            RepositoryChannelStore store = myRepositoryChannelsService.getRepositoryByChannel(channel);

            store.iteratePluginNodes(pluginNode -> allPluginIds.add(pluginNode.id));
        }

        for (PlatformNodeDesc desc : PlatformNodeDesc.values()) {
            allPluginIds.remove(desc.id());
            for (String oldId : desc.oldIds()) {
                allPluginIds.remove(oldId);
            }
        }

        for (PluginChannel channel : PluginChannel.values()) {
            RepositoryChannelStore store = myRepositoryChannelsService.getRepositoryByChannel(channel);

            for (String allPluginId : allPluginIds) {
                RepositoryNodeState state = store.getState(allPluginId);
                if (state == null) {
                    continue;
                }
                
                for (String toRemoveBuild : toRemoveBuilds) {
                    NavigableSet<PluginNode> pluginNodes = state.getPluginsByPlatformVersion().get(toRemoveBuild);
                    if (pluginNodes == null) {
                        continue;
                    }

                    for (PluginNode node : pluginNodes) {
                        filesToRemove.add(Objects.requireNonNull(node.targetPath));

                        state.remove(node.version, node.platformVersion);
                    }
                }
            }
        }

        logger.info("CleanUp: Analyzing repos - marked {} platform builds to remove. All {} files marked to remove with plugins", toRemoveBuilds.size(), filesToRemove.size());

        long startTime = System.currentTimeMillis();
        filesToRemove.parallelStream().forEach(path -> {
            try {
                Files.deleteIfExists(path);

                Path jsonFile = path.getParent().resolve(path.getFileName() + ".json");

                Files.deleteIfExists(jsonFile);
            } catch (IOException e) {
                logger.error("Failed to remove " + path, e);
            }
        });
        
        long endTime = (System.currentTimeMillis() - startTime) / 1000L;

        logger.info("CleanUp: Finished to remove files in {} seconds", endTime);
    }
}
