package org.mustbe.consulo.war.servlet;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.output.StringBuilderWriter;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.war.util.ApplicationConfiguration;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.IdeaPluginDescriptorImpl;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.diagnostic.DefaultLogger;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.MultiMap;
import com.intellij.util.io.ZipUtil;

/**
 * @author VISTALL
 * @since 21.04.14
 */
public class PluginManager
{
	private static class MyLogger extends DefaultLogger
	{
		public MyLogger(String category)
		{
			super(category);
		}

		@Override
		public void info(String message)
		{
			infoOrWarn(message, null);
		}

		@Override
		public void info(String message, Throwable t)
		{
			infoOrWarn(message, t);
		}

		@Override
		public void warn(@NonNls String message)
		{
			infoOrWarn(message, null);
		}

		@Override
		public void warn(@NonNls String message, Throwable t)
		{
			infoOrWarn(message, t);
		}

		public void infoOrWarn(String message, @Nullable Throwable t, String... details)
		{
			System.out.println(message);
			if(t != null)
			{
				t.printStackTrace();
			}
			if(details != null && details.length > 0)
			{
				System.out.println("details: ");
				for(String detail : details)
				{
					System.out.println(detail);
				}
			}
		}
	}

	private static class MyLoggerFactory implements Logger.Factory
	{
		@Override
		public Logger getLoggerInstance(String s)
		{
			return new MyLogger(s);
		}
	}

	private static final String EMPTY = "<plugin-repository></plugin-repository>";

	public static final PluginManager INSTANCE = new PluginManager();

	private String xmlRepoText;

	private PluginManager()
	{
		Logger.setFactory(MyLoggerFactory.class);
		new Thread()
		{
			@Override
			public void run()
			{
				while(true)
				{
					xmlRepoText = null;

					try
					{
						Thread.sleep(30 * 60 * 1000L);
					}
					catch(InterruptedException e)
					{
						//
					}
				}
			}
		}.start();
	}

	private static String getRepoXmlFile()
	{
		String vulcanWorkDir = ApplicationConfiguration.getProperty("vulcan.dir");
		if(vulcanWorkDir == null)
		{
			return EMPTY;
		}

		File vulcanWorkDirFile = new File(vulcanWorkDir, "work");

		Map<IdeaPluginDescriptor, String> pluginToBuildProject = new HashMap<IdeaPluginDescriptor, String>();
		List<IdeaPluginDescriptorImpl> list = new ArrayList<IdeaPluginDescriptorImpl>();

		File[] listOfWorkDir = vulcanWorkDirFile.listFiles();
		for(File file : listOfWorkDir)
		{
			File distOut = new File(file, "out/artifacts/dist");
			if(!distOut.exists())
			{
				continue;
			}

			List<IdeaPluginDescriptorImpl> newList = new ArrayList<IdeaPluginDescriptorImpl>();
			PluginManagerCore.loadDescriptors(distOut.getAbsolutePath(), newList, null, listOfWorkDir.length);

			list.addAll(newList);
			for(IdeaPluginDescriptorImpl ideaPluginDescriptor : newList)
			{
				pluginToBuildProject.put(ideaPluginDescriptor, file.getName());
			}
		}

		File pluginsZip = new File(ApplicationConfiguration.getProperty("consulo.plugins.work.dir"));
		FileUtil.createDirectory(pluginsZip);

		Map<IdeaPluginDescriptorImpl, File> files = new HashMap<IdeaPluginDescriptorImpl, File>();
		for(IdeaPluginDescriptorImpl ideaPluginDescriptor : list)
		{
			File path = ideaPluginDescriptor.getPath();

			try
			{
				File zipIoFile = new File(pluginsZip, ideaPluginDescriptor.getPluginId().getIdString() + ".zip");

				if(zipIoFile.exists())
				{
					zipIoFile.delete();
				}

				ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipIoFile));
				ZipUtil.addDirToZipRecursively(out, zipIoFile, path, path.getName(), null, null);
				out.close();

				files.put(ideaPluginDescriptor, zipIoFile);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}

		MultiMap<String, IdeaPluginDescriptorImpl> pluginsByCategory = new MultiMap<String, IdeaPluginDescriptorImpl>();

		for(IdeaPluginDescriptorImpl ideaPluginDescriptor : list)
		{
			if(!files.containsKey(ideaPluginDescriptor))
			{
				continue;
			}
			String category = ideaPluginDescriptor.getCategory() == null ? "None" : ideaPluginDescriptor.getCategory();

			pluginsByCategory.putValue(StringUtil.capitalize(category), ideaPluginDescriptor);
		}

		Document document = DocumentHelper.createDocument();
		Element root = document.addElement("plugin-repository");

		for(Map.Entry<String, Collection<IdeaPluginDescriptorImpl>> entry : pluginsByCategory.entrySet())
		{
			Element categoryElement = root.addElement("category");
			categoryElement.addAttribute("name", entry.getKey());

			for(IdeaPluginDescriptorImpl pluginDescriptor : entry.getValue())
			{
				String buildProject = pluginToBuildProject.get(pluginDescriptor);

				Element ideaPluginElement = categoryElement.addElement("idea-plugin");
				ideaPluginElement.addAttribute("downloads", String.valueOf(0));
				ideaPluginElement.addAttribute("size", String.valueOf(files.get(pluginDescriptor).length()));
				ideaPluginElement.addAttribute("date", String.valueOf(System.currentTimeMillis()));
				ideaPluginElement.addAttribute("url", "");

				ideaPluginElement.addElement("id").setText(pluginDescriptor.getPluginId().getIdString());
				ideaPluginElement.addElement("name").setText(pluginDescriptor.getName().trim());
				ideaPluginElement.addElement("description").setText(StringUtil.notNullize(pluginDescriptor.getDescription()).trim());
				String version = pluginDescriptor.getVersion();
				if(StringUtil.isNotEmpty(version))
				{
					ideaPluginElement.addElement("version").setText(version.trim());
				}
				else
				{
					ideaPluginElement.addElement("version").setText("UNDEFINED");
				}
				String vendor = pluginDescriptor.getVendor();
				if(StringUtil.isNotEmpty(vendor))
				{
					ideaPluginElement.addElement("vendor").setText(vendor.trim());
				}

				for(PluginId pluginId : pluginDescriptor.getDependentPluginIds())
				{
					if(ArrayUtil.contains(pluginId, pluginDescriptor.getOptionalDependentPluginIds()))
					{
						continue;
					}
					Element dependElement = ideaPluginElement.addElement("depends");
					dependElement.setText(pluginId.getIdString());
				}

				for(PluginId pluginId : pluginDescriptor.getOptionalDependentPluginIds())
				{
					Element dependElement = ideaPluginElement.addElement("depends");
					dependElement.setText(pluginId.getIdString());
					dependElement.addAttribute("optional", "true");
				}

				ideaPluginElement.addElement("rating").setText(String.valueOf(0));
				if(buildProject != null)
				{
					ideaPluginElement.addElement("build-project").setText(buildProject);
				}
			}
		}

		String text = EMPTY;
		try
		{
			StringBuilderWriter writer = new StringBuilderWriter();
			XMLWriter xmlWriter = new XMLWriter(writer, OutputFormat.createPrettyPrint());
			xmlWriter.write(document);
			writer.close();

			text = writer.getBuilder().toString();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return text;
	}

	public synchronized String getXmlRepoText()
	{
		if(xmlRepoText != null)
		{
			return xmlRepoText;
		}
		xmlRepoText = getRepoXmlFile();
		return xmlRepoText;
	}
}
