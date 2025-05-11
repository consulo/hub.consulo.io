package consulo.app.plugins.frontend.backend;

import consulo.hub.shared.repository.PluginNode;

import java.util.List;
import java.util.Map;

/**
 * @author VISTALL
 * @since 2025-05-11
 */
public record PluginsCache(List<PluginNode> sortedByDownloads, Map<String, PluginNode> mappped) {
    public boolean isValid() {
        return !sortedByDownloads().isEmpty();
    }
}
