package consulo.hub.backend.repository.analyzer.builtin;

import com.fasterxml.jackson.databind.ObjectMapper;
import consulo.hub.backend.repository.analyzer.PluginAnalyzerEnv;
import consulo.hub.backend.repository.analyzer.PluginAnalyzerRunner;
import consulo.hub.backend.repository.analyzer.PluginAnalyzerRunnerFactory;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2024-08-04
 */
public class BuiltinPluginAnalyzerRunnerFactory implements PluginAnalyzerRunnerFactory {
    private final ObjectMapper myObjectMapper;

    public BuiltinPluginAnalyzerRunnerFactory(ObjectMapper objectMapper) {
        myObjectMapper = objectMapper;
    }

    @Nonnull
    @Override
    public PluginAnalyzerRunner create(PluginAnalyzerEnv env) {
        return new BuiltinPluginAnalyzerRunner(env, myObjectMapper);
    }
}
