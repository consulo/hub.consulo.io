package consulo.hub.backend.repository.analyzer;

import consulo.hub.shared.repository.PluginNode;

/**
 * @author VISTALL
 * @since 2024-08-04
 */
public interface PluginAnalyzerRunner {
    PluginNode.ExtensionPreview[] run(String targetPluginId, String[] pluginsDir) throws Exception;
}
