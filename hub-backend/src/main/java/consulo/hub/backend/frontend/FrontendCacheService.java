package consulo.hub.backend.frontend;

import consulo.hub.backend.repository.PluginStatisticsService;
import consulo.hub.backend.repository.RepositoryChannelStore;
import consulo.hub.backend.repository.RepositoryChannelsService;
import consulo.hub.shared.repository.FrontPluginNode;
import consulo.hub.shared.repository.FrontPluginNodeById;
import consulo.hub.shared.repository.FrontPluginVersionInfo;
import consulo.hub.shared.repository.PluginChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author VISTALL
 * @since 23/05/2023
 */
@Service
public class FrontendCacheService {
    private record PluginIdAndVersion(String id, String version) {
    }

    private final RepositoryChannelsService myRepositoryChannelsService;
    private final PluginStatisticsService myPluginStatisticsService;

    @Autowired
    public FrontendCacheService(RepositoryChannelsService repositoryChannelsService, PluginStatisticsService pluginStatisticsService) {
        myRepositoryChannelsService = repositoryChannelsService;
        myPluginStatisticsService = pluginStatisticsService;
    }

    public Collection<FrontPluginNodeById> listPluginsById() {
        Map<String, FrontPluginNodeById> map = new HashMap<>();
        
        for (PluginChannel channel : PluginChannel.values()) {
            RepositoryChannelStore service = myRepositoryChannelsService.getRepositoryByChannel(channel);

            service.iteratePluginNodes(pluginNode -> {
                FrontPluginNodeById nodeById = map.computeIfAbsent(pluginNode.id, s -> {
                    FrontPluginNodeById node = new FrontPluginNodeById(pluginNode);
                    node.myDownloads = myPluginStatisticsService.getDownloadStatCountAll(pluginNode.id);
                    return node;
                });

                FrontPluginVersionInfo versionInfo =
                    nodeById.myVersions.computeIfAbsent(pluginNode.version, FrontPluginVersionInfo::new);

                versionInfo.myDate = pluginNode.date;

                versionInfo.myChannels.add(channel);
            });
        }

        return map.values();
    }

    public Collection<FrontPluginNode> listPlugins() {
        Map<PluginIdAndVersion, FrontPluginNode> map = new HashMap<>();
        for (PluginChannel channel : PluginChannel.values()) {
            RepositoryChannelStore service = myRepositoryChannelsService.getRepositoryByChannel(channel);

            service.iteratePluginNodes(pluginNode -> {
                FrontPluginNode node = map.computeIfAbsent(new PluginIdAndVersion(pluginNode.id, pluginNode.version), pluginIdAndVersion ->
                {
                    FrontPluginNode frontPluginNode = new FrontPluginNode();
                    frontPluginNode.myPluginNode = pluginNode.clone();

                    int countAll = myPluginStatisticsService.getDownloadStatCountAll(pluginNode.id);

                    frontPluginNode.myPluginNode.downloads = countAll;
                    frontPluginNode.myPluginNode.downloadsAll = countAll;
                    return frontPluginNode;
                });

                node.myChannels.add(channel);
            });
        }

        return map.values();
    }
}
