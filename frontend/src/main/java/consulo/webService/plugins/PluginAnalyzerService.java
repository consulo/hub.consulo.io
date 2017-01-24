package consulo.webService.plugins;

import gnu.trove.THashMap;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jdom.Document;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.picocontainer.PicoContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.google.common.collect.Lists;
import com.intellij.ide.plugins.IdeaPluginDescriptorImpl;
import com.intellij.lang.Language;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.psi.SingleRootFileViewProvider;
import com.intellij.util.ArrayUtil;
import com.intellij.util.PathUtil;
import com.intellij.util.ThrowableConsumer;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.MultiMap;
import com.intellij.util.io.ZipUtil;
import consulo.pluginAnalyzer.Analyzer;
import consulo.ui.Component;
import consulo.webService.UserConfigurationService;

/**
 * @author VISTALL
 * @since 20-Sep-16
 */
@Service
public class PluginAnalyzerService
{
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
		addUrlByClass(Component.class);
		// platform-api
		addUrlByClass("com.intellij.execution.configurations.ConfigurationType");
		// platform-impl
		addUrlByClass("consulo.extension.impl.ModuleExtensionImpl");
		// external-system-api
		addUrlByClass("com.intellij.openapi.externalSystem.model.ExternalProject");
		// external-system-impl
		addUrlByClass("com.intellij.openapi.externalSystem.action.AttachExternalProjectAction");
		// extensions
		addUrlByClass(PluginId.class);
		// picocontainer
		addUrlByClass(PicoContainer.class);
		// util
		addUrlByClass(ContainerUtil.class);
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
		catch(ClassNotFoundException | java.net.MalformedURLException e)
		{
			LOGGER.error("Class " + clazzName + " is not found", e);
		}
	}

	@NotNull
	public MultiMap<String, String> analyze(IdeaPluginDescriptorImpl ideaPluginDescriptor, PluginChannelService channelService, String[] dependencies) throws Exception
	{
		MultiMap<String, Element> extensions = ideaPluginDescriptor.getExtensions();
		if(extensions == null)
		{
			return MultiMap.empty();
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

		MultiMap<String, String> data = new MultiMap<String, String>()
		{
			@NotNull
			@Override
			protected Map<String, Collection<String>> createMap()
			{
				return new TreeMap<>();
			}

			@NotNull
			@Override
			protected Collection<String> createCollection()
			{
				return new TreeSet<>();
			}
		};

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

								Object configurationType = constructorForNew.newInstance();

								String id = (String) configurationTypeIdMethod.invoke(configurationType);

								data.putValue(key, id);
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

								Set<String> ext = new TreeSet<>();

								Method analyzeFileType = analyzerClass.getDeclaredMethod("analyzeFileType", Set.class, fileTypeFactoryClass);
								try
								{
									analyzeFileType.invoke(null, ext, fileTypeFactory);
								}
								catch(Throwable e)
								{
									// somebodies can insert foreign logic in factory (com.intellij.xml.XmlFileTypeFactory:38)
									// it can failed, but - before logic, extensions can be registered
									//LOGGER.error(e);
								}

								if(!ext.isEmpty())
								{
									data.putValues(key, ext);
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
								data.putValue(key, extensionKey);
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

		return data;
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
