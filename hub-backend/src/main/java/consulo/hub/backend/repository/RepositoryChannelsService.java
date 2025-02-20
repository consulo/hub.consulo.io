package consulo.hub.backend.repository;

import consulo.hub.shared.repository.PluginChannel;
import consulo.hub.shared.repository.PluginNode;
import consulo.hub.shared.repository.util.PlatformNodeDesc;
import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 18/05/2023
 */
public interface RepositoryChannelsService {
    @Nonnull
    RepositoryChannelStore getRepositoryByChannel(@Nonnull PluginChannel channel);

    @Nonnull
    default String getDeployPluginExtension() {
        return RepositoryChannelStore.PLUGIN_EXTENSION;
    }

    default void init() throws Exception {
    }

    @Nonnull
    default String getNodeExtension(@Nonnull PluginNode pluginNode) {
        PlatformNodeDesc node = PlatformNodeDesc.getNode(pluginNode.id);
        if (node == null) {
            return getDeployPluginExtension();
        }
        return node.ext();
    }

    @Nonnull
    default String getNodeContentType(@Nonnull PluginNode pluginNode) {
        PlatformNodeDesc node = PlatformNodeDesc.getNode(pluginNode.id);
        if (node == null) {
            return "application/zip";
        }

        String ext = node.ext();
        switch (ext) {
            case "exe":
                return "application/vnd.microsoft.portable-executable";
            case "zip":
                return "application/zip";
        }
        return "application/x-gtar";
    }
}
