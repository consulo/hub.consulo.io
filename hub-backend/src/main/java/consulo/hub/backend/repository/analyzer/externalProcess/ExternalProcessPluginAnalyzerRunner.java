package consulo.hub.backend.repository.analyzer.externalProcess;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import consulo.hub.backend.TempFileService;
import consulo.hub.backend.repository.analyzer.PluginAnalyzerEnv;
import consulo.hub.backend.repository.analyzer.PluginAnalyzerRunner;
import consulo.hub.pluginAnalyzer.container.ContainerMain;
import consulo.hub.pluginAnalyzer.container.ContainerRunData;
import consulo.hub.shared.repository.PluginNode;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author VISTALL
 * @since 2024-08-04
 */
public class ExternalProcessPluginAnalyzerRunner implements PluginAnalyzerRunner {
    private final PluginAnalyzerEnv myEnv;
    private final TempFileService myTempFileService;
    private final ObjectMapper myObjectMapper;

    public ExternalProcessPluginAnalyzerRunner(PluginAnalyzerEnv env,
                                               TempFileService tempFileService,
                                               ObjectMapper objectMapper) {
        myEnv = env;
        myTempFileService = tempFileService;
        myObjectMapper = objectMapper;
    }

    @Override
    public PluginNode.ExtensionPreview[] run(String targetPluginId, String[] pluginsDir) throws Exception {
        List<URL> classUrls = myEnv.getContainerGroup().getClassUrls();

        List<String> classPaths = new ArrayList<>();
        for (URL classUrl : classUrls) {
            File file = new File(classUrl.toURI());

            classPaths.add(file.getAbsolutePath());
        }

        Path input = myTempFileService.createTempFilePath("input", "json");
        Path output = myTempFileService.createTempFilePath("output", "json");

        ContainerRunData containerRunData = new ContainerRunData();
        containerRunData.setPlatformURLs(myEnv.getPlatformClassGroup().getClassUrls());
        containerRunData.setAnalyzerURLs(myEnv.getAnalyzerClassGroup().getClassUrls());
        containerRunData.setPluginsDir(pluginsDir);
        containerRunData.setTargetPluginId(targetPluginId);

        Files.write(input, myObjectMapper.writeValueAsBytes(containerRunData));

        String classPath = String.join(File.pathSeparator, classPaths);

        List<String> processParameters = new ArrayList<>();
        processParameters.add(getJavaPath());
        processParameters.add("-classpath");
        processParameters.add(classPath);
        processParameters.add(ContainerMain.class.getName());
        processParameters.add(input.toAbsolutePath().toString());
        processParameters.add(output.toAbsolutePath().toString());

        ProcessBuilder builder = new ProcessBuilder(processParameters);
        builder.redirectErrorStream(true);
        builder.inheritIO();

        Process process = builder.start();

        if (!process.waitFor(10, TimeUnit.SECONDS)) {
            process.destroyForcibly();
        }

        try {
            List<Map<String, String>> result = myObjectMapper.readValue(output.toFile(), new TypeReference<List<Map<String, String>>>() {
            });

            return myObjectMapper.convertValue(result, PluginNode.ExtensionPreview[].class);
        }
        catch (Exception ignored) {
        }

        return new PluginNode.ExtensionPreview[0];
    }

    private static String getJavaPath() {
        String javaExe = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        if (SystemUtils.IS_OS_WINDOWS) {
            javaExe += ".exe";
        }
        return javaExe;
    }
}
