package org.mustbe.consulo.war.plugins;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipOutputStream;

import org.jdom.Document;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.war.util.ApplicationConfiguration;
import org.mustbe.consulo.war.util.ConsuloHelper;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.IdeaPluginDescriptorImpl;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.MultiMap;
import com.intellij.util.io.ZipUtil;

/**
 * @author VISTALL
 * @since 22.04.14
 */
public class SnapshotPluginDirManager extends PluginDirManager
{
	private String myXmlList;

	public SnapshotPluginDirManager(File file)
	{
		super(file);

		ConsuloHelper.init();

		new Thread()
		{
			@Override
			public void run()
			{
				while(true)
				{
					myXmlList = null;

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

	private static String generate()
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

		File pluginsZip = new File(ApplicationConfiguration.getProperty("consulo.plugins.work.dir"), "SNAPSHOT");
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

		Document document = new Document();
		Element root = new Element("plugin-repository");
		document.addContent(root);

		for(Map.Entry<String, Collection<IdeaPluginDescriptorImpl>> entry : pluginsByCategory.entrySet())
		{
			Element categoryElement = new Element("category");
			root.addContent(categoryElement);
			categoryElement.setAttribute("name", entry.getKey());

			for(IdeaPluginDescriptorImpl pluginDescriptor : entry.getValue())
			{
				String buildProject = pluginToBuildProject.get(pluginDescriptor);

				Element ideaPluginElement = new Element("idea-plugin");
				categoryElement.addContent(ideaPluginElement);
				ideaPluginElement.setAttribute("downloads", String.valueOf(0));
				ideaPluginElement.setAttribute("size", String.valueOf(files.get(pluginDescriptor).length()));
				ideaPluginElement.setAttribute("date", String.valueOf(System.currentTimeMillis()));
				ideaPluginElement.setAttribute("url", "");

				ideaPluginElement.addContent(new Element("id").setText(pluginDescriptor.getPluginId().getIdString()));
				ideaPluginElement.addContent(new Element("name").setText(pluginDescriptor.getName().trim()));
				ideaPluginElement.addContent(new Element("description").setText(StringUtil.notNullize(pluginDescriptor.getDescription()).trim()));
				String version = pluginDescriptor.getVersion();
				ideaPluginElement.addContent(new Element("version").setText(StringUtil.isEmpty(version) ? "SNAPSHOT" : version.trim()));
				String vendor = pluginDescriptor.getVendor();
				if(StringUtil.isNotEmpty(vendor))
				{
					ideaPluginElement.addContent(new Element("vendor").setText(vendor.trim()));
				}

				for(PluginId pluginId : pluginDescriptor.getDependentPluginIds())
				{
					if(ArrayUtil.contains(pluginId, pluginDescriptor.getOptionalDependentPluginIds()))
					{
						continue;
					}
					ideaPluginElement.addContent(new Element("depends").setText(pluginId.getIdString()));
				}

				ideaPluginElement.addContent(new Element("rating").setText(String.valueOf(0)));
				if(buildProject != null)
				{
					ideaPluginElement.addContent(new Element("build-project").setText(buildProject));
				}
			}
		}

		String text = EMPTY;
		try
		{
			text = JDOMUtil.writeDocument(document, "\n");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return text;
	}

	@NotNull
	@Override
	public String getXmlListText()
	{
		if(myXmlList != null)
		{
			return myXmlList;
		}
		myXmlList = generate();
		return myXmlList;
	}
}
