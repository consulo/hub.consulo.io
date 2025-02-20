package consulo.webservice.stub;

import consulo.hub.backend.repository.analyzer.PluginAnalyzerEnv;
import consulo.hub.backend.repository.analyzer.PluginAnalyzerRunner;
import consulo.hub.backend.repository.analyzer.PluginAnalyzerRunnerFactory;
import consulo.hub.shared.repository.PluginNode;
import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2025-02-19
 */
public class StubPluginAnalyzerRunnerFactory implements PluginAnalyzerRunnerFactory {
    @Nonnull
    @Override
    public PluginAnalyzerRunner create(PluginAnalyzerEnv env) {
        return (targetPluginId, pluginsDir) -> new PluginNode.ExtensionPreview[0];
    }
}
