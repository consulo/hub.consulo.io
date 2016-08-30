package consulo.webService.update.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.io.ByteStreams;
import com.intellij.ide.plugins.IdeaPluginDescriptorImpl;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.CommonProcessors;
import com.intellij.util.io.ZipUtil;
import consulo.webService.RootService;
import consulo.webService.ServiceIsNotReadyException;
import consulo.webService.update.PluginChannelService;
import consulo.webService.update.PluginNode;
import consulo.webService.update.UpdateChannel;
import consulo.webService.util.GsonUtil;

/**
 * @author VISTALL
 * @since 28-Aug-16
 */
@WebServlet(urlPatterns = {"/v2/plugins/deploy"})
public class PluginsDeployServlet extends HttpServlet
{
	private static final Logger LOGGER = LoggerFactory.getLogger(PluginsDeployServlet.class);

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		resp.sendError(HttpServletResponse.SC_FORBIDDEN);
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		try
		{
			String channelParameter = req.getParameter("channel");
			UpdateChannel channel = channelParameter == null ? null : UpdateChannel.valueOf(channelParameter);
			if(channel == null)
			{
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}

			RootService rootService = RootService.getInstance();

			File tempFile = rootService.createTempFile("deploy", "zip");

			try (ServletInputStream inputStream = req.getInputStream())
			{
				try (OutputStream output = new FileOutputStream(tempFile))
				{
					ByteStreams.copy(inputStream, output);
				}
			}

			File deployUnzip = rootService.createTempFile("deploy_unzip", "");
			FileUtilRt.createDirectory(deployUnzip);

			ZipUtil.extract(tempFile, deployUnzip, null);

			loadPlugin(rootService, channel, deployUnzip);

			FileUtilRt.delete(tempFile);
			FileUtilRt.delete(deployUnzip);
		}
		catch(ServiceIsNotReadyException e)
		{
			resp.sendRedirect("/v2/status");
		}
		catch(Exception e)
		{
			LOGGER.error(e.getMessage(), e);

			resp.sendError(HttpServletResponse.SC_FORBIDDEN);
		}
	}

	private void loadPlugin(RootService rootService, UpdateChannel channel, File deployUnzip) throws IOException
	{
		List<IdeaPluginDescriptorImpl> pluginDescriptors = new ArrayList<IdeaPluginDescriptorImpl>();
		PluginManagerCore.loadDescriptors(deployUnzip.getAbsolutePath(), pluginDescriptors, null, 1);
		if(pluginDescriptors.size() != 1)
		{
			throw new IllegalArgumentException("Bad plugin [" + pluginDescriptors.size() + "]");
		}

		IdeaPluginDescriptorImpl ideaPluginDescriptor = pluginDescriptors.get(0);

		PluginNode pluginNode = new PluginNode();
		pluginNode.id = ideaPluginDescriptor.getPluginId().getIdString();
		pluginNode.version = stableVersion(ideaPluginDescriptor.getVersion());
		pluginNode.name = ideaPluginDescriptor.getName();
		pluginNode.category = ideaPluginDescriptor.getCategory();
		pluginNode.description = ideaPluginDescriptor.getDescription();
		pluginNode.date = System.currentTimeMillis();
		pluginNode.vendor = ideaPluginDescriptor.getVendor();

		pluginNode.dependencies = Arrays.stream(ideaPluginDescriptor.getDependentPluginIds()).map(PluginId::getIdString).toArray(String[]::new);
		pluginNode.optionalDependencies = Arrays.stream(ideaPluginDescriptor.getOptionalDependentPluginIds()).map(PluginId::getIdString).toArray(String[]::new);
		pluginNode.sinceConsuloBuild = stableVersion(ideaPluginDescriptor.getSinceBuild());

		PluginChannelService pluginChannelService = rootService.getUpdateService(channel);

		pluginChannelService.push(pluginNode, f -> {
			try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(f)))
			{
				CommonProcessors.CollectProcessor<File> fileCollectProcessor = new CommonProcessors.CollectProcessor<>();
				File ideaPluginDescriptorPath = ideaPluginDescriptor.getPath();
				FileUtil.visitFiles(ideaPluginDescriptorPath, fileCollectProcessor);

				for(File child : fileCollectProcessor.getResults())
				{
					if(child.isDirectory())
					{
						continue;
					}

					String relativePath = FileUtilRt.getRelativePath(ideaPluginDescriptorPath, child);

					ZipEntry zipEntry = new ZipEntry(pluginNode.id + "/" + relativePath);
					zipOutputStream.putNextEntry(zipEntry);

					try (FileInputStream fileOutputStream = new FileInputStream(child))
					{
						ByteStreams.copy(fileOutputStream, zipOutputStream);
					}

					zipOutputStream.closeEntry();
				}
			}

			pluginNode.length = f.length();
			pluginNode.targetFile = f;

			File metaFile = new File(f.getParentFile(), f.getName() + ".json");
			FileUtilRt.delete(metaFile);

			FileUtil.writeToFile(metaFile, GsonUtil.get().toJson(pluginNode));
		});
	}

	private static String stableVersion(String value)
	{
		if(StringUtil.isEmpty(value) || "SNAPSHOT".equals(value))
		{
			throw new IllegalArgumentException("Empty or snapshot version is not acceptable");
		}
		return value;
	}
}
