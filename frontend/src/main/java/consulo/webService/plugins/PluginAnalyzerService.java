package consulo.webService.plugins;

import com.google.common.collect.Lists;
import com.intellij.ide.plugins.IdeaPluginDescriptorImpl;
import com.intellij.lang.Language;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.SingleRootFileViewProvider;
import com.intellij.util.ArrayUtil;
import com.intellij.util.PathUtil;
import com.intellij.util.ThrowableConsumer;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.ContainerUtilRt;
import com.intellij.util.containers.MultiMap;
import com.intellij.util.io.ZipUtil;
import consulo.disposer.internal.impl.DisposerInternalImpl;
import consulo.pluginAnalyzer.Analyzer;
import consulo.webService.UserConfigurationService;
import gnu.trove.THashMap;
import org.jdom.Document;
import org.jdom.Element;
import org.picocontainer.PicoContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * @author VISTALL
 * @since 20-Sep-16
 */
@Service
public class PluginAnalyzerService
{
	private static class TreeMultiMap<K, V> extends MultiMap<K, V>
	{
		@Nonnull
		@Override
		protected Map<K, Collection<V>> createMap()
		{
			return new TreeMap<>();
		}

		@Nonnull
		@Override
		protected Collection<V> createCollection()
		{
			return new TreeSet<>();
		}

		@Override
		public void putValues(K key, @Nonnull Collection<? extends V> values)
		{
			if(values.isEmpty())
			{
				return;
			}

			super.putValues(key, values);
		}
	}

	public static class ExtensionsResult
	{
		public MultiMap<String, String> v1 = MultiMap.create();
		public MultiMap<String, String> v2 = MultiMap.create();
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(PluginAnalyzerService.class);

	private final List<URL> platformClassUrls = new ArrayList<>();

	private UserConfigurationService myUserConfigurationService;

	@Autowired
	public PluginAnalyzerService(UserConfigurationService userConfigurationService)
	{
		myUserConfigurationService = userConfigurationService;

		init();
	}

	private void init()
	{
		// core-api
		addUrlByClass(Language.class);
		// core-impl
		addUrlByClass(SingleRootFileViewProvider.class);
		// ui-api
		addUrlByClass("consulo.ui.Component");
		// platform-api
		addUrlByClass("com.intellij.openapi.fileTypes.FileTypeConsumer");
		// platform-impl
		addUrlByClass("com.intellij.concurrency.ApplierCompleter");
		// lang-api
		addUrlByClass("com.intellij.execution.configurations.ConfigurationType");
		// lang-impl
		addUrlByClass("com.intellij.execution.configuration.ConfigurationFactoryEx");
		// compiler-api
		addUrlByClass("com.intellij.packaging.artifacts.ArtifactType");
		// compiler-impl
		addUrlByClass("com.intellij.packaging.impl.elements.ArchivePackagingElement");
		// project-model-api
		addUrlByClass("com.intellij.openapi.roots.ui.configuration.ModulesProvider");
		// project-model-impl
		addUrlByClass("consulo.extension.impl.ModuleExtensionImpl");
		// external-system-api
		addUrlByClass("com.intellij.openapi.externalSystem.model.ExternalProject");
		// external-system-impl
		addUrlByClass("com.intellij.openapi.externalSystem.action.AttachExternalProjectAction");
		// injeting-api
		addUrlByClass("consulo.injecting.InjectingContainerOwner");
		// injecting-pico-impl
		addUrlByClass("consulo.injecting.pico.PicoInjectingContainer");
		// test-impl
		addUrlByClass("consulo.test.light.LightApplicationBuilder");
		// editor-ex
		addUrlByClass("com.intellij.ide.ui.UISettings");
		// javax.inject
		addUrlByClass(Inject.class);
		// extensions
		addUrlByClass(PluginId.class);
		// picocontainer
		addUrlByClass(PicoContainer.class);
		// util
		addUrlByClass(ContainerUtil.class);
		// util-rt
		addUrlByClass(ContainerUtilRt.class);
		// disposer-api
		addUrlByClass(Disposable.class);
		// disposer-impl
		addUrlByClass(DisposerInternalImpl.class);
		// jdom
		addUrlByClass(Document.class);
		// trove4j
		addUrlByClass(THashMap.class);
		// guava
		addUrlByClass(Lists.class);
		// plugin-analyzer-rt
		addUrlByClass(Analyzer.class);
	}

	private void addUrlByClass(Class<?> clazz)
	{
		addUrlByClass(clazz.getName());
	}

	private void addUrlByClass(String clazzName)
	{
		try
		{
			Class<?> clazz = Class.forName(clazzName);

			String jarPathForClass = PathUtil.getJarPathForClass(clazz);

			platformClassUrls.add(new File(jarPathForClass).toURI().toURL());
		}
		catch(ClassNotFoundException | MalformedURLException e)
		{
			LOGGER.error("Class " + clazzName + " is not found", e);
		}
	}

	@Nonnull
	public ExtensionsResult analyze(IdeaPluginDescriptorImpl ideaPluginDescriptor, PluginChannelService channelService, String[] dependencies) throws Exception
	{
		MultiMap<String, Element> extensions = ideaPluginDescriptor.getExtensions();
		if(extensions == null)
		{
			return new ExtensionsResult();
		}

		List<URL> urls = new ArrayList<>();
		urls.addAll(platformClassUrls);

		File[] forRemove = new File[0];
		for(String dependencyId : dependencies)
		{
			PluginNode pluginNode = channelService.select(PluginChannelService.SNAPSHOT, dependencyId, null, false);
			if(pluginNode == null)
			{
				continue;
			}

			File analyzeUnzip = myUserConfigurationService.createTempFile("analyze_unzip", "");
			forRemove = ArrayUtil.append(forRemove, analyzeUnzip);

			ZipUtil.extract(pluginNode.targetFile, analyzeUnzip, null);

			File libFile = new File(analyzeUnzip, dependencyId + "/lib");
			File[] files = libFile.listFiles((dir, name) -> name.endsWith(".jar"));
			if(files != null)
			{
				for(File file : files)
				{
					urls.add(file.toURI().toURL());
				}
			}
		}

		for(File file : ideaPluginDescriptor.getClassPath())
		{
			urls.add(file.toURI().toURL());
		}

		MultiMap<String, String> extensionsV1 = new TreeMultiMap<>();
		MultiMap<String, String> extensionsV2 = new TreeMultiMap<>();

		try (URLClassLoader urlClassLoader = URLClassLoader.newInstance(urls.toArray(new URL[urls.size()]), null))
		{
			Class<?> analyzerClass = urlClassLoader.loadClass(Analyzer.class.getName());
			analyzerClass.getDeclaredMethod("before").invoke(null);

			Class<?> configurationTypeClass = urlClassLoader.loadClass("com.intellij.execution.configurations.ConfigurationType");
			Method configurationTypeIdMethod = configurationTypeClass.getDeclaredMethod("getId");

			for(Map.Entry<String, Collection<Element>> entry : extensions.entrySet())
			{
				String key = entry.getKey();
				switch(key)
				{
					case "com.intellij.configurationType":
						forEachQuiet(entry, element ->
						{
							String implementation = element.getAttributeValue("implementation");
							if(implementation != null)
							{
								Class<?> aClass = urlClassLoader.loadClass(implementation);

								Constructor constructorForNew = null;
								Constructor<?>[] declaredConstructors = aClass.getDeclaredConstructors();
								for(Constructor<?> declaredConstructor : declaredConstructors)
								{
									if(declaredConstructor.getParameterCount() == 0)
									{
										declaredConstructor.setAccessible(true);
										constructorForNew = declaredConstructor;
									}
								}

								if(constructorForNew == null)
								{
									return;
								}

								constructorForNew.setAccessible(true);

								Object configurationType = constructorForNew.newInstance();

								String id = (String) configurationTypeIdMethod.invoke(configurationType);

								extensionsV1.putValue(key, id);
								extensionsV2.putValue(key, id);
							}
						});
						break;
					case "com.intellij.vcs":
						forEachQuiet(entry, element ->
						{
							String extensionKey = element.getAttributeValue("name");
							if(extensionKey != null)
							{
								extensionsV1.putValue(key, extensionKey);
								extensionsV2.putValue(key, extensionKey);
							}
						});
						break;
					case "com.intellij.fileTypeFactory":
						forEachQuiet(entry, element ->
						{
							String implementation = element.getAttributeValue("implementation");
							if(implementation != null)
							{
								Class<?> aClass = urlClassLoader.loadClass(implementation);

								Object fileTypeFactory = aClass.newInstance();

								Class<?> fileTypeFactoryClass = urlClassLoader.loadClass("com.intellij.openapi.fileTypes.FileTypeFactory");

								Set<String> extV1 = new TreeSet<>();
								Set<String> extV2 = new TreeSet<>();

								Method analyzeFileType = analyzerClass.getDeclaredMethod("analyzeFileType", Set.class, Set.class, fileTypeFactoryClass);
								try
								{
									analyzeFileType.invoke(null, extV1, extV2, fileTypeFactory);
								}
								catch(Throwable e)
								{
									// somebodies can insert foreign logic in factory (com.intellij.xml.XmlFileTypeFactory:38)
									// it can failed, but - before logic, extensions can be registered
									//LOGGER.error(e.getMessage(), e);
								}

								extensionsV1.putValues(key, extV1);
								extensionsV2.putValues(key, extV2);
							}
						});
						break;
					case "com.intellij.packaging.artifactType":
						forEachQuiet(entry, element ->
						{
							String implementation = element.getAttributeValue("implementation");
							if(implementation != null)
							{
								Class<?> aClass = urlClassLoader.loadClass(implementation);

								Object artifactInstance = aClass.newInstance();

								Class<?> artifactType = urlClassLoader.loadClass("com.intellij.packaging.artifacts.ArtifactType");

								Method idMethod = artifactType.getMethod("getId");
								idMethod.setAccessible(true);

								String artifactId = (String) idMethod.invoke(artifactInstance);
								if(!StringUtil.isEmpty(artifactId))
								{
									extensionsV1.putValue(key, artifactId);
									extensionsV2.putValue(key, artifactId);
								}
							}
						});
						break;
					case "com.intellij.moduleExtensionProvider":
						forEachQuiet(entry, element ->
						{
							String extensionKey = element.getAttributeValue("key");
							if(extensionKey != null)
							{
								extensionsV1.putValue(key, extensionKey);
								extensionsV2.putValue(key, extensionKey);
							}
						});
						break;
				}
			}

			analyzerClass.getDeclaredMethod("after").invoke(null);
		}
		finally
		{
			myUserConfigurationService.asyncDelete(forRemove);
		}

		ExtensionsResult extensionsResult = new ExtensionsResult();
		extensionsResult.v1 = extensionsV1;
		extensionsResult.v2 = extensionsV2;
		return extensionsResult;
	}

	private static void forEachQuiet(Map.Entry<String, Collection<Element>> entry, ThrowableConsumer<Element, Throwable> consumer)
	{
		for(Element element : entry.getValue())
		{
			try
			{
				consumer.consume(element);
			}
			catch(Throwable e)
			{
				LOGGER.info(e.getMessage(), e);
			}
		}
	}
}
