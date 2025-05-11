package consulo.app.plugins.frontend.backend;

import consulo.hub.shared.repository.PluginNode;

import java.util.List;

/**
 * @author VISTALL
 * @since 2025-05-11
 */
public record PluginsCache(List<PluginNode> sortedByDownloads) {
    public boolean isValid() {
        return !sortedByDownloads().isEmpty();
    }
}
