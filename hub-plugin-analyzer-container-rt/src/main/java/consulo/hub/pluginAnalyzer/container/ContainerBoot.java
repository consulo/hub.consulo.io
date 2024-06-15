package consulo.hub.pluginAnalyzer.container;

import consulo.container.boot.ContainerPathManager;
import consulo.container.impl.PluginDescriptorImpl;
import consulo.container.impl.PluginHolderModificator;
import consulo.container.impl.classloader.PluginClassLoaderImpl;
import consulo.container.impl.classloader.PluginLoadStatistics;
import consulo.container.internal.PathManagerHolder;
import consulo.container.plugin.PluginId;
import consulo.container.plugin.PluginIds;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Set;

/**
 * @author VISTALL
 * @since 06/05/2023
 */
public class ContainerBoot
{
	// called by reflection
	public static Object init(List<URL> platformURLs, List<URL> analyzerURLs, String[] pluginsDir, String targetPluginId) throws Exception
	{
		// disable ignore plugins check
		System.setProperty("consulo.ignore.disabled.plugins", "true");

		PluginLoadStatistics.initialize(false);

		File workDir = new File("");

		PathManagerHolder.setInstance(new ContainerPathManager()
		{
			@Override
			public String getHomePath()
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public File getAppHomeDirectory()
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public String getConfigPath()
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public String getSystemPath()
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public File getDocumentsDir()
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public String[] getPluginsPaths()
			{
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

		return analyzerClass.getDeclaredMethod("runAnalyzer", String.class).invoke(null, targetPluginId);
	}

	private static PluginDescriptorImpl initPlugin(PluginId pluginId, List<URL> urls, ClassLoader[] parentClassLoaders, final File workDir)
	{
		PluginDescriptorImpl basePlatformPlugin = new PluginDescriptorImpl(workDir, new byte[0], new byte[0], true)
		{
			@Override
			public String getName()
			{
				return pluginId.getIdString();
			}

			@Override
			public PluginId getPluginId()
			{
				return pluginId;
			}

			@Override
			public List<File> getClassPath(Set<PluginId> enabledPluginIds)
			{
				throw new UnsupportedOperationException();
			}
		};

		PluginClassLoaderImpl baseClassLoader = new PluginClassLoaderImpl(urls, parentClassLoaders, basePlatformPlugin);
		basePlatformPlugin.setLoader(baseClassLoader);

		return basePlatformPlugin;
	}
}
