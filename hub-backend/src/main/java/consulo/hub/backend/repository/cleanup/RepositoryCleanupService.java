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
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author VISTALL
 * @since 2025-02-09
 */
@Service
public class RepositoryCleanupService {
    public static final int ourMaxBuildCount = 10;

    private static final Logger logger = LoggerFactory.getLogger(RepositoryCleanupService.class);

    private final RepositoryChannelsService myRepositoryChannelsService;

    public RepositoryCleanupService(RepositoryChannelsService repositoryChannelsService) {
        myRepositoryChannelsService = repositoryChannelsService;
    }

    public void collect() {
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

        Set<BuildNumber> allVersions = new TreeSet<>();

        // collect all data
        for (PlatformNodeDesc desc : PlatformNodeDesc.values()) {
            Map<PluginChannel, TreeSet<BuildNumber>> map = versions.getOrDefault(desc, Map.of());

            if (map.size() != pluginChannels.length) {
                versions.remove(desc);
            } else {
                for (TreeSet<BuildNumber> set : map.values()) {
                    allVersions.addAll(set);
                }
            }
        }

        for (PlatformNodeDesc desc : PlatformNodeDesc.values()) {
            Map<PluginChannel, TreeSet<BuildNumber>> map = versions.getOrDefault(desc, Map.of());

            for (Map.Entry<PluginChannel, TreeSet<BuildNumber>> entry : map.entrySet()) {
                PluginChannel channel = entry.getKey();
                TreeSet<BuildNumber> deployedVersions = entry.getValue();

                Iterator<BuildNumber> allVersionsIterator = allVersions.iterator();
                while (allVersionsIterator.hasNext()) {
                    BuildNumber allVersion = allVersionsIterator.next();

                    if (!deployedVersions.contains(allVersion)) {
                        allVersionsIterator.remove();

                        System.out.println("skip " + allVersion + " not in " + channel + " channed");
                    }
                }
            }
        }


//        Map<String, PluginsStateOld> pluginStates = pluginChannelService.copyPluginsState();
//        // first of all we need check platform nodes
//        for (String platformPluginId : RepositoryUtil.ourPlatformPluginIds) {
//            PluginsStateOld pluginsState = pluginStates.get(platformPluginId);
//            if (pluginsState == null) {
//                continue;
//            }
//
//            NavigableMap<String, NavigableSet<PluginNode>> map = pluginsState.getPluginsByPlatformVersion();
//
//            int i = map.size();
//            for (Map.Entry<String, NavigableSet<PluginNode>> entry : map.entrySet()) {
//                String platformVersion = entry.getKey();
//
//                if (i > ourMaxBuildCount) {
//                    outdatedPlatformVersions.add(platformVersion);
//                    NavigableSet<PluginNode> value = entry.getValue();
//                    if (!value.isEmpty()) {
//                        toRemove.add(value.iterator().next());
//                    }
//
//                    i--;
//                }
//            }
//        }
    }
}
