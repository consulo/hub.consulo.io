package consulo.hub.pluginAnalyzer.container;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * @author VISTALL
 * @since 2024-08-04
 */
public class ContainerRunData {
    private List<URL> platformURLs;
    private List<URL> analyzerURLs;
    private String[] pluginsDir;
    private String targetPluginId;

    public List<URL> getPlatformURLs() {
        return platformURLs;
    }

    public void setPlatformURLs(List<URL> platformURLs) {
        this.platformURLs = platformURLs;
    }

    public List<URL> getAnalyzerURLs() {
        return analyzerURLs;
    }

    public void setAnalyzerURLs(List<URL> analyzerURLs) {
        this.analyzerURLs = analyzerURLs;
    }

    public String[] getPluginsDir() {
        return pluginsDir;
    }

    public void setPluginsDir(String[] pluginsDir) {
        this.pluginsDir = pluginsDir;
    }

    public String getTargetPluginId() {
        return targetPluginId;
    }

    public void setTargetPluginId(String targetPluginId) {
        this.targetPluginId = targetPluginId;
    }

    @Override
    public String toString() {
        return "ContainerRunData{" +
            "platformURLs=" + platformURLs +
            ", analyzerURLs=" + analyzerURLs +
            ", pluginsDir=" + (pluginsDir == null ? null : Arrays.asList(pluginsDir)) +
            ", targetPluginId='" + targetPluginId + '\'' +
            '}';
    }
}
