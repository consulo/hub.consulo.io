package consulo.webService.update.pluginAnalyzer;

import gnu.trove.THashMap;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.jdom.Document;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.picocontainer.PicoContainer;
import com.google.common.collect.Lists;
import com.intellij.ide.plugins.IdeaPluginDescriptorImpl;
import com.intellij.lang.Language;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.psi.SingleRootFileViewProvider;
import com.intellij.util.PathUtil;
import com.intellij.util.ThrowableConsumer;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.MultiMap;
import com.intellij.util.lang.UrlClassLoader;
import consulo.pluginAnalyzer.Analyzer;
import consulo.webService.ChildService;

/**
 * @author VISTALL
 * @since 20-Sep-16
 */
public class PluginAnalyzerService extends ChildService
{
	private static final Logger LOGGER = Logger.getInstance(PluginAnalyzerService.class);

	private final List<URL> platformClassUrls = new ArrayList<>();

	@Override
	protected void initImpl(File pluginChannelDir)
	{
		// core-api
		addUrlByClass(Language.class);
		// core-impl
		addUrlByClass(SingleRootFileViewProvider.class);
		// platform-api
		addUrlByClass("com.intellij.execution.configurations.ConfigurationType");
		// platform-impl
		addUrlByClass("consulo.extension.impl.ModuleExtensionImpl");
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

	public void analyze(IdeaPluginDescriptorImpl ideaPluginDescriptor) throws Exception
	{
		List<URL> urls = new ArrayList<>();
		urls.addAll(platformClassUrls);

		File path = ideaPluginDescriptor.getPath();

		for(File file : ideaPluginDescriptor.getClassPath())
		{
			urls.add(file.toURI().toURL());
		}

		UrlClassLoader urlClassLoader = UrlClassLoader.build().urls(urls).useCache(false).get();

		Class<?> analyzerClass = urlClassLoader.loadClass(Analyzer.class.getName());
		analyzerClass.getDeclaredMethod("before").invoke(null);

		MultiMap<String, String> data = new MultiMap<String, String>()
		{
			@NotNull
			@Override
			protected Collection<String> createCollection()
			{
				return new TreeSet<>();
			}
		};

		MultiMap<String, Element> extensions = ideaPluginDescriptor.getExtensions();
		if(extensions == null)
		{
			return;
		}

		for(Map.Entry<String, Collection<Element>> entry : extensions.entrySet())
		{
			String key = entry.getKey();
			switch(key)
			{
				case "com.intellij.configurationType":
					invokeSilent(entry, element -> {
						String implementation = element.getAttributeValue("implementation");
						if(implementation != null)
						{
							Class<?> aClass = urlClassLoader.loadClass(implementation);

							Object configurationType = aClass.newInstance();

							Method getId = aClass.getDeclaredMethod("getId");
							String id = (String) getId.invoke(configurationType);

							data.putValue(key, id);
						}
					});
					break;
				case "com.intellij.fileTypeFactory":
					invokeSilent(entry, element -> {
						String implementation = element.getAttributeValue("implementation");
						if(implementation != null)
						{
							Class<?> aClass = urlClassLoader.loadClass(implementation);

							Object fileTypeFactory = aClass.newInstance();

							Class<?> fileTypeFactoryClass = Class.forName("com.intellij.openapi.fileTypes.FileTypeFactory", true, urlClassLoader);

							Method analyzeFileType = analyzerClass.getDeclaredMethod("analyzeFileType", Set.class, fileTypeFactoryClass);

							Set<String> ext = new TreeSet<>();

							analyzeFileType.invoke(null, ext, fileTypeFactory);

							data.putValues(key, ext);
						}
					});
					break;
				case "com.intellij.moduleExtensionProvider":
					invokeSilent(entry, element -> {
						String extensionKey = element.getAttributeValue("key");
						if(extensionKey != null)
						{
							data.putValue(key, extensionKey);
						}
					});
					break;
			}
		}

		System.out.println("test");
	}

	private static void invokeSilent(Map.Entry<String, Collection<Element>> entry, ThrowableConsumer<Element, Exception> consumer)
	{
		for(Element element : entry.getValue())
		{
			try
			{
				consumer.consume(element);
			}
			catch(Exception e)
			{
				LOGGER.info(e);
			}
		}
	}
}
