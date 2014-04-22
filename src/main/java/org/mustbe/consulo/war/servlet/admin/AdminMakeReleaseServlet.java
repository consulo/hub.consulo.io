package org.mustbe.consulo.war.servlet.admin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.Document;
import org.mustbe.consulo.war.PluginDirManager;
import org.mustbe.consulo.war.PluginManagerNew;
import org.mustbe.consulo.war.SystemAvailable;
import org.mustbe.consulo.war.util.ApplicationConfiguration;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.util.io.FileUtil;

/**
 * @author VISTALL
 * @since 22.04.14
 */
public class AdminMakeReleaseServlet extends HttpServlet
{
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		if(!SystemAvailable.INSTANCE.get())
		{
			resp.getWriter().write("Processing...");
			return;
		}

		try
		{
			SystemAvailable.INSTANCE.set(false);

			File file = new File(ApplicationConfiguration.getProperty("vulcan.dir"), "work/consulo");
			if(!file.exists())
			{
				resp.getWriter().write("Consulo project is missing");
				return;
			}

			int buildNumber = resolveBuildNumber(file);
			if(buildNumber == -1)
			{
				resp.getWriter().write("Consulo project is not build");
				return;
			}

			if(!copyConsuloDist(resp, file, buildNumber))
			{
				return;
			}

			copyPlugins(buildNumber);

			PluginManagerNew.INSTANCE.addPluginBuild(buildNumber);

			resp.getWriter().println("Done");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			SystemAvailable.INSTANCE.set(true);
		}
	}

	private void copyPlugins(int buildNumber) throws Exception
	{
		PluginDirManager snapshotPluginDirManager = PluginManagerNew.INSTANCE.findPluginDir(Integer.MAX_VALUE);

		// when we call it - it ill generate new list and plugins
		String xmlListText = snapshotPluginDirManager.getXmlListText();

		File workDir = PluginManagerNew.INSTANCE.getWorkDir();

		File pluginDirWithBuild = new File(workDir, String.valueOf(buildNumber));

		FileUtil.writeToFile(new File(pluginDirWithBuild, "list.xml"), xmlListText);

		FileUtil.copyDir(snapshotPluginDirManager.getDir(), pluginDirWithBuild);
	}

	private boolean copyConsuloDist(HttpServletResponse resp, File file, int buildNumber) throws IOException
	{
		File releaseDir = new File(ApplicationConfiguration.getProperty("consulo.release.work.dir"));
		releaseDir.mkdirs();

		File ourDir = new File(releaseDir, String.valueOf(buildNumber));
		if(ourDir.exists())
		{
			resp.getWriter().write("Build: " + buildNumber + " already released");
			return false;
		}

		FileUtil.copyDir(new File(file, "out/artifacts/dist"), ourDir);
		return true;
	}

	private int resolveBuildNumber(File file)
	{
		File resourcesJar = new File(file, "out/artifacts/distForRuntime/lib/resources.jar");
		if(!resourcesJar.exists())
		{
			return -1;
		}

		try
		{
			ZipFile zipFile = new ZipFile(resourcesJar);
			ZipEntry entry = zipFile.getEntry("idea/ConsuloApplicationInfo.xml");
			if(entry == null)
			{
				return -1;
			}
			InputStream inputStream = zipFile.getInputStream(entry);

			Document document = JDOMUtil.loadDocument(inputStream);
			return PluginManagerNew.toBuild(document.getRootElement().getChild("build").getAttributeValue("number"));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return -1;
		}
	}
}
