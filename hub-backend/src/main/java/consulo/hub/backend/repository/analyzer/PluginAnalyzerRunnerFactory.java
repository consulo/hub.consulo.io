package consulo.hub.backend.repository.analyzer;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2024-08-04
 */
public interface PluginAnalyzerRunnerFactory {
    @Nonnull
    PluginAnalyzerRunner create(PluginAnalyzerEnv env);
}
