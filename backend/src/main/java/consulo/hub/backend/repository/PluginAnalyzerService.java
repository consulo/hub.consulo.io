package consulo.hub.backend.repository;

import com.google.common.collect.Lists;
import com.intellij.lang.Language;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.SingleRootFileViewProvider;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ThrowableConsumer;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.MultiMap;
import com.intellij.util.io.URLUtil;
import consulo.container.impl.PluginDescriptorImpl;
import consulo.container.impl.classloader.PluginClassLoaderFactory;
import consulo.container.impl.parser.ExtensionInfo;
import consulo.container.plugin.PluginId;
import consulo.disposer.Disposable;
import consulo.disposer.internal.impl.DisposerInternalImpl;
import consulo.hub.backend.util.ZipUtil;
import consulo.hub.shared.repository.PluginNode;
import consulo.pluginAnalyzer.Analyzer;
import consulo.util.collection.primitive.ints.IntMaps;
import consulo.util.concurrent.AsyncResult;
import consulo.util.dataholder.UserDataHolder;
import consulo.util.lang.ObjectUtil;
import consulo.util.nodep.classloader.UrlClassLoader;
import consulo.util.nodep.map.SimpleMultiMap;
import consulo.util.nodep.xml.node.SimpleXmlElement;
import org.jdom.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.util.*;
import java.util.zip.ZipFile;

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

	private PluginChannelsService myUserConfigurationService;

	@Autowired
	public PluginAnalyzerService(PluginChannelsService userConfigurationService)
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
		// execution-api
		addUrlByClass("com.intellij.execution.configurations.ConfigurationType");
		// lang-api
		addUrlByClass("com.intellij.lang.CompositeLanguage");
		// lang-impl
		addUrlByClass("com.intellij.execution.configuration.ConfigurationFactoryEx");
		// compiler-api
		addUrlByClass("com.intellij.packaging.artifacts.ArtifactType");
		// compiler-impl
		addUrlByClass("com.intellij.packaging.impl.elements.ArchivePackagingElement");
		// project-model-api
		addUrlByClass("com.intellij.openapi.roots.ui.configuration.ModulesProvider");
		// project-model-impl
		addUrlByClass("consulo.module.extension.impl.ModuleExtensionImpl");
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
		// logging-api
		addUrlByClass("consulo.logging.Logger");
		addUrlByClass("org.slf4j.LoggerFactory");
		// logging-impl
		addUrlByClass("consulo.logging.internal.LoggerFactory");
		// jakarta.inject
		addUrlByClass("jakarta.inject.Inject");
		// container-api
		addUrlByClass(PluginId.class);
		// container-impl
		addUrlByClass(PluginClassLoaderFactory.class);
		// util
		addUrlByClass(ContainerUtil.class);
		// util-collection
		addUrlByClass(consulo.util.collection.ContainerUtil.class);
		// util-collection-primitive
		addUrlByClass(IntMaps.class);
		// util-collection-via-trove
		addUrlByClass("consulo.util.collection.trove.impl.TroveCollectionFactory");
		// trove4j
		addUrlByClass("gnu.trove.THashMap");
		// util-lang
		addUrlByClass(ObjectUtil.class);
		// util-serializer
		addUrlByClass("com.intellij.util.xmlb.XmlSerializerImpl");
		// icon library
		addUrlByClass("consulo.platform.base.icon.PlatformIconGroup");
		// util-jdom
		addUrlByClass("consulo.util.jdom.JDOMUtil");
		// util-io
		addUrlByClass("consulo.util.io.URLUtil");
		// util-concurrent
		addUrlByClass(AsyncResult.class);
		// util-dataholder
		addUrlByClass(UserDataHolder.class);
		// util-nodep
		addUrlByClass(UrlClassLoader.class);
		// disposer-api
		addUrlByClass(Disposable.class);
		// disposer-impl
		addUrlByClass(DisposerInternalImpl.class);
		// jdom
		addUrlByClass(Document.class);
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

			File jarPathForClass = getJarPathForClass(clazz);

			platformClassUrls.add(jarPathForClass.toURI().toURL());
		}
		catch(ClassNotFoundException | MalformedURLException e)
		{
			LOGGER.error("Class " + clazzName + " is not found", e);
		}
	}

	@Nonnull
	private static File getJarPathForClass(@Nonnull Class aClass)
	{
		CodeSource codeSource = aClass.getProtectionDomain().getCodeSource();
		if(codeSource != null)
		{
			URL location = codeSource.getLocation();
			if(location != null)
			{
				return URLUtil.urlToFile(location);
			}
		}
		throw new IllegalArgumentException("can't find path for class " + aClass.getName());
	}

	@Nonnull
	public ExtensionsResult analyze(PluginDescriptorImpl ideaPluginDescriptor, PluginChannelService channelService, String[] dependencies) throws Exception
	{
		SimpleMultiMap<String, ExtensionInfo> extensions = ideaPluginDescriptor.getExtensions();
		if(extensions.isEmpty())
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

			try(ZipFile zipFile = new ZipFile(pluginNode.targetFile))
			{
				ZipUtil.extract(zipFile, analyzeUnzip);
			}

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

			for(Map.Entry<String, Collection<ExtensionInfo>> entry : extensions.entrySet())
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

								Object configurationType = newInstance(aClass);

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
					case "com.intellij.fileType":
						forEachQuiet(entry, element ->
						{
							String exts = element.getAttributeValue("extensions");
							
							List<String> extsAsList = StringUtil.split(StringUtil.notNullize(exts), ";");
							for(String ext : extsAsList)
							{
								extensionsV1.putValue(key, ext);
								extensionsV2.putValue(key, "*|" + ext);
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

								Object fileTypeFactory = newInstance(aClass);

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
									e.printStackTrace();
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

								Object artifactInstance = newInstance(aClass);

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

	@Nonnull
	private static Object newInstance(Class<?> clazz) throws Exception
	{
		Constructor constructorForNew = null;

		Constructor<?>[] declaredConstructors = clazz.getDeclaredConstructors();
		for(Constructor<?> declaredConstructor : declaredConstructors)
		{
			if(declaredConstructor.getParameterCount() == 0)
			{
				declaredConstructor.setAccessible(true);
				constructorForNew = declaredConstructor;
				break;
			}
		}

		if(constructorForNew == null)
		{
			throw new IllegalArgumentException("no empty constructor");
		}
		return constructorForNew.newInstance();
	}

	private static void forEachQuiet(Map.Entry<String, Collection<ExtensionInfo>> entry, ThrowableConsumer<SimpleXmlElement, Throwable> consumer)
	{
		for(ExtensionInfo element : entry.getValue())
		{
			try
			{
				consumer.consume(element.getElement());
			}
			catch(Throwable e)
			{
				LOGGER.info(e.getMessage(), e);
			}
		}
	}
}
