package consulo.hub.backend.repository;

import com.google.common.collect.Lists;
import consulo.container.plugin.PluginId;
import consulo.disposer.Disposable;
import consulo.disposer.internal.impl.DisposerInternalImpl;
import consulo.hub.backend.util.ZipUtil;
import consulo.hub.shared.repository.PluginNode;
import consulo.pluginAnalyzer.Analyzer;
import consulo.util.collection.ArrayUtil;
import consulo.util.collection.ContainerUtil;
import consulo.util.collection.MultiMap;
import consulo.util.collection.primitive.ints.IntMaps;
import consulo.util.concurrent.AsyncResult;
import consulo.util.dataholder.UserDataHolder;
import consulo.util.io.StreamUtil;
import consulo.util.io.URLUtil;
import consulo.util.lang.ObjectUtil;
import consulo.util.lang.StringUtil;
import consulo.util.nodep.classloader.UrlClassLoader;
import consulo.util.nodep.map.SimpleMultiMap;
import consulo.util.nodep.xml.node.SimpleXmlElement;
import org.apache.commons.lang3.SystemUtils;
import org.intellij.lang.annotations.Language;
import org.jdom.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.security.CodeSource;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author VISTALL
 * @since 20-Sep-16
 */
@Service
@Order(2_000)
public class PluginAnalyzerService implements CommandLineRunner
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

	private static final Logger LOG = LoggerFactory.getLogger(PluginAnalyzerService.class);

	private final List<URL> myPlatformClassUrls = new ArrayList<>();

	private PluginChannelsService myUserConfigurationService;

	private Set<String> myRequiredClasses = new LinkedHashSet<>();

	@Autowired
	public PluginAnalyzerService(PluginChannelsService userConfigurationService)
	{
		myUserConfigurationService = userConfigurationService;

		fillClasses();
	}

	private void fillClasses()
	{
		// core-api
		addRequiredClass(Language.class);
		// core-impl
		addRequiredClass(SingleRootFileViewProvider.class);
		// ui-api
		addRequiredClass("consulo.ui.Component");
		// platform-api
		addRequiredClass("com.intellij.openapi.fileTypes.FileTypeConsumer");
		// platform-impl
		addRequiredClass("com.intellij.concurrency.ApplierCompleter");
		// execution-api
		addRequiredClass("com.intellij.execution.configurations.ConfigurationType");
		// lang-api
		addRequiredClass("com.intellij.lang.CompositeLanguage");
		// lang-impl
		addRequiredClass("com.intellij.execution.configuration.ConfigurationFactoryEx");
		// compiler-api
		addRequiredClass("com.intellij.packaging.artifacts.ArtifactType");
		// compiler-impl
		addRequiredClass("com.intellij.packaging.impl.elements.ArchivePackagingElement");
		// project-model-api
		addRequiredClass("com.intellij.openapi.roots.ui.configuration.ModulesProvider");
		// project-model-impl
		addRequiredClass("consulo.module.extension.impl.ModuleExtensionImpl");
		// external-system-api
		addRequiredClass("com.intellij.openapi.externalSystem.model.ExternalProject");
		// external-system-impl
		addRequiredClass("com.intellij.openapi.externalSystem.action.AttachExternalProjectAction");
		// injeting-api
		addRequiredClass("consulo.injecting.InjectingContainerOwner");
		// injecting-pico-impl
		addRequiredClass("consulo.injecting.pico.PicoInjectingContainer");
		// test-impl
		addRequiredClass("consulo.test.light.LightApplicationBuilder");
		// editor-ex
		addRequiredClass("com.intellij.ide.ui.UISettings");
		// logging-api
		addRequiredClass("consulo.logging.Logger");
		addRequiredClass("org.slf4j.LoggerFactory");
		// logging-impl
		addRequiredClass("consulo.logging.internal.LoggerFactory");
		// jakarta.inject
		addRequiredClass("jakarta.inject.Inject");
		// container-api
		addRequiredClass(PluginId.class);
		// container-impl
		addRequiredClass(PluginClassLoaderFactory.class);
		// util
		addRequiredClass(ContainerUtil.class);
		// util-collection
		addRequiredClass(consulo.util.collection.ContainerUtil.class);
		// util-collection-primitive
		addRequiredClass(IntMaps.class);
		// util-collection-via-trove
		addRequiredClass("consulo.util.collection.trove.impl.TroveCollectionFactory");
		// trove4j
		addRequiredClass("gnu.trove.THashMap");
		// util-lang
		addRequiredClass(ObjectUtil.class);
		// util-serializer
		addRequiredClass("com.intellij.util.xmlb.XmlSerializerImpl");
		// icon library
		addRequiredClass("consulo.platform.base.icon.PlatformIconGroup");
		// util-jdom
		addRequiredClass("consulo.util.jdom.JDOMUtil");
		// util-io
		addRequiredClass("consulo.util.io.URLUtil");
		// util-concurrent
		addRequiredClass(AsyncResult.class);
		// util-dataholder
		addRequiredClass(UserDataHolder.class);
		// util-nodep
		addRequiredClass(UrlClassLoader.class);
		// disposer-api
		addRequiredClass(Disposable.class);
		// disposer-impl
		addRequiredClass(DisposerInternalImpl.class);
		// jdom
		addRequiredClass(Document.class);
		// guava
		addRequiredClass(Lists.class);
		// plugin-analyzer-rt
		addRequiredClass(Analyzer.class);
	}

	@Override
	public void run(String[] args) throws Exception
	{
		URL urlLang = getJarUrlForClass(Language.class);

		if(urlLang.toString().contains("BOOT-INF"))
		{
			prepareRunningInsideBoot();
		}
		else
		{
			prepareRunningOutsideBoot();
		}
	}

	private void prepareRunningInsideBoot() throws Exception
	{
		File librariesTempDir = myUserConfigurationService.createTempDir("plugin-analyzer-core");

		Map<String, ZipFile> zipFileMap = new HashMap<>();

		for(String requiredClass : myRequiredClasses)
		{
			Class<?> clazz = Class.forName(requiredClass);

			URL url = getJarUrlForClass(clazz);

			//jar:file:/W:/_github.com/consulo/hub.consulo.io/backend/target/hub-backend-1.0-SNAPSHOT.jar!/BOOT-INF/lib/consulo-core-api-2-SNAPSHOT.jar!/

			String urlString = url.toString();

			if(urlString.endsWith("!/"))
			{
				urlString = urlString.substring(0, urlString.length() - 2);
			}

			int firstSeparator = urlString.indexOf("!/");

			// we need start slash
			String bootJarFile = urlString.substring(SystemUtils.IS_OS_WINDOWS ? 10 : 9, firstSeparator);

			String jarEntry = urlString.substring(firstSeparator + 2, urlString.length());

			ZipFile zipFile = zipFileMap.computeIfAbsent(bootJarFile, it -> {
				try
				{
					return new ZipFile(it, StandardCharsets.UTF_8);
				}
				catch(IOException e)
				{
					throw new RuntimeException(e);
				}
			});

			ZipEntry entry = zipFile.getEntry(jarEntry);

			String jarName = jarEntry.substring(jarEntry.lastIndexOf("/") + 1, jarEntry.length());
			File targetJarFile = new File(librariesTempDir, jarName);
			try (InputStream inputStream = zipFile.getInputStream(entry); FileOutputStream stream = new FileOutputStream(targetJarFile))
			{
				StreamUtil.copyStreamContent(inputStream, stream);
			}

			myPlatformClassUrls.add(targetJarFile.toURI().toURL());
		}

		for(ZipFile file : zipFileMap.values())
		{
			try
			{
				file.close();
			}
			catch(IOException ignored)
			{
			}
		}
	}

	private void prepareRunningOutsideBoot()
	{
		for(String clazzName : myRequiredClasses)
		{
			try
			{
				Class<?> clazz = Class.forName(clazzName);

				File jarPathForClass = getJarPathForClass(clazz);

				myPlatformClassUrls.add(jarPathForClass.toURI().toURL());
			}
			catch(ClassNotFoundException | MalformedURLException e)
			{
				LOG.error("Class " + clazzName + " is not found", e);
			}
		}
	}

	private void addRequiredClass(Class<?> clazz)
	{
		addRequiredClass(clazz.getName());
	}

	private void addRequiredClass(String clazzName)
	{
		myRequiredClasses.add(clazzName);
	}

	@Nonnull
	private static URL getJarUrlForClass(@Nonnull Class aClass)
	{
		CodeSource codeSource = aClass.getProtectionDomain().getCodeSource();
		if(codeSource != null)
		{
			URL location = codeSource.getLocation();
			if(location != null)
			{
				return location;
			}
		}
		throw new IllegalArgumentException("can't find path for class " + aClass.getName());
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
		urls.addAll(myPlatformClassUrls);

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

			try (ZipFile zipFile = new ZipFile(pluginNode.targetFile))
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
				LOG.info(e.getMessage(), e);
			}
		}
	}
}
