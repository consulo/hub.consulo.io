package consulo.hub.pluginAnalyzer.container;

import consulo.container.boot.ContainerPathManager;
import consulo.container.impl.ClassPathItem;
import consulo.container.impl.PluginDescriptorImpl;
import consulo.container.impl.PluginHolderModificator;
import consulo.container.impl.classloader.PluginClassLoaderImpl;
import consulo.container.internal.PathManagerHolder;
import consulo.container.plugin.*;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author VISTALL
 * @since 06/05/2023
 */
public class ContainerBoot {
    // called by reflection
    @SuppressWarnings("unchecked")
    public static List<Map<String, String>> init(List<URL> platformURLs, List<URL> analyzerURLs, String[] pluginsDir, String targetPluginId) throws Exception {
        // disable ignore plugins check
        System.setProperty("consulo.ignore.disabled.plugins", "true");

        File workDir = new File("");

        PathManagerHolder.setInstance(new ContainerPathManager() {
            @Override
            public String getHomePath() {
                throw new UnsupportedOperationException();
            }

            @Override
            public File getAppHomeDirectory() {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getConfigPath() {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getSystemPath() {
                throw new UnsupportedOperationException();
            }

            @Override
            public File getDocumentsDir() {
                throw new UnsupportedOperationException();
            }

            @Override
            public String[] getPluginsPaths() {
                return pluginsDir;
            }
        });

        PluginDescriptorImpl base = initPlugin(PluginIds.CONSULO_BASE, platformURLs, new ClassLoader[]{ContainerBoot.class.getClassLoader()}, workDir);

        /**
         * @see PluginIds#CONSULO_REPO_ANALYZER
         */
        PluginId analyzerPluginId = PluginId.getId("consulo.repo.analyzer");

        PluginDescriptorImpl analyzer = initPlugin(analyzerPluginId, analyzerURLs, new ClassLoader[]{base.getPluginClassLoader()}, workDir);

        PluginHolderModificator.initialize(List.of(base, analyzer));

        Class<?> analyzerClass = Class.forName("consulo.hub.pluginAnalyzer.Analyzer", true, analyzer.getPluginClassLoader());

        List<Map<String, String>> data = (List<Map<String, String>>) analyzerClass.getDeclaredMethod("runAnalyzer", String.class).invoke(null, targetPluginId);

        closeAllPlugins();

        return data;
    }

    private static void closeAllPlugins() {
        ArrayList<PluginDescriptor> descriptors = new ArrayList<>(PluginManager.getPlugins());

        System.out.println("Disposing: " + descriptors.stream().map(it -> it.getPluginId().getIdString()).collect(Collectors.joining(", ")));

        for (PluginDescriptor descriptor : descriptors) {
            if (descriptor.getStatus() != PluginDescriptorStatus.OK) {
                continue;
            }

            ClassLoader pluginClassLoader = descriptor.getPluginClassLoader();
            if (pluginClassLoader instanceof PluginClassLoaderImpl pluginClassLoaderImpl) {
                try {
                    ((PluginClassLoaderImpl) pluginClassLoader).close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }

            ((PluginDescriptorImpl) descriptor).setLoader(null);
            ((PluginDescriptorImpl) descriptor).setModuleLayer(null);
            ((PluginDescriptorImpl) descriptor).setStatus(PluginDescriptorStatus.ERROR_WHILE_LOADING);
        }

        PluginHolderModificator.initialize(List.of());
    }

    private static PluginDescriptorImpl initPlugin(PluginId pluginId, List<URL> urls, ClassLoader[] parentClassLoaders, final File workDir) {
        PluginDescriptorImpl basePlatformPlugin = new PluginDescriptorImpl(workDir, new byte[0], new byte[0], true) {
            @Override
            public String getName() {
                return pluginId.getIdString();
            }

            @Override
            public PluginId getPluginId() {
                return pluginId;
            }

            @Override
            public List<ClassPathItem> getClassPathItems(Set<PluginId> enabledPluginIds) {
                throw new UnsupportedOperationException();
            }
        };
        basePlatformPlugin.setStatus(PluginDescriptorStatus.OK);

        PluginClassLoaderImpl baseClassLoader = new PluginClassLoaderImpl(urls, null, parentClassLoaders, basePlatformPlugin);
        basePlatformPlugin.setLoader(baseClassLoader);

        return basePlatformPlugin;
    }
}
