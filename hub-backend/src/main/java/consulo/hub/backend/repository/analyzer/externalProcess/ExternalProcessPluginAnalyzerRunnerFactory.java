package consulo.hub.backend.repository.analyzer.externalProcess;

import com.fasterxml.jackson.databind.ObjectMapper;
import consulo.hub.backend.TempFileService;
import consulo.hub.backend.repository.analyzer.PluginAnalyzerEnv;
import consulo.hub.backend.repository.analyzer.PluginAnalyzerRunnerFactory;
import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2024-08-04
 */
public class ExternalProcessPluginAnalyzerRunnerFactory implements PluginAnalyzerRunnerFactory {
    private final ObjectMapper myObjectMapper;
    private final TempFileService myTempFileService;

    public ExternalProcessPluginAnalyzerRunnerFactory(ObjectMapper objectMapper, TempFileService tempFileService) {
        myObjectMapper = objectMapper;
        myTempFileService = tempFileService;
    }

    @Nonnull
    @Override
    public ExternalProcessPluginAnalyzerRunner create(PluginAnalyzerEnv env) {
        return new ExternalProcessPluginAnalyzerRunner(env, myTempFileService, myObjectMapper);
    }
}
