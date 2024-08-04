package consulo.hub.pluginAnalyzer.container;

import com.google.gson.Gson;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * @author VISTALL
 * @since 2024-08-04
 */
public class ContainerMain {
    public static void main(String[] args) throws Exception {
        String inputFile = args[0];
        String outputFile = args[1];

        Gson gson = new Gson();

        ContainerRunData runData = gson.fromJson(Files.readString(Path.of(inputFile)), ContainerRunData.class);

        List<Map<String, String>> result = ContainerBoot.init(runData.getPlatformURLs(), runData.getAnalyzerURLs(), runData.getPluginsDir(), runData.getTargetPluginId());

        String json = gson.toJson(result);

        Files.writeString(Path.of(outputFile), json);
    }
}
