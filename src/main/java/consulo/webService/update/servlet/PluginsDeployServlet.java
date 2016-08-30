package consulo.webService.update.servlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.io.ByteStreams;
import com.intellij.ide.plugins.IdeaPluginDescriptorImpl;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.util.io.ZipUtil;
import consulo.webService.RootService;
import consulo.webService.ServiceIsNotReadyException;
import consulo.webService.update.PluginChannelService;
import consulo.webService.update.UpdateChannel;

/**
 * @author VISTALL
 * @since 28-Aug-16
 */
@WebServlet(urlPatterns = {"/v2/plugins/deploy"})
public class PluginsDeployServlet extends HttpServlet
{
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

			PluginChannelService pluginChannelService = rootService.getUpdateService(channel);

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

			loadPlugin(deployUnzip);

			FileUtilRt.delete(tempFile);
			FileUtilRt.delete(deployUnzip);
		}
		catch(ServiceIsNotReadyException e)
		{
			resp.sendRedirect("/v2/status");
		}
	}

	private void loadPlugin(File deployUnzip)
	{
		List<IdeaPluginDescriptorImpl> pluginDescriptors = new ArrayList<IdeaPluginDescriptorImpl>();
		PluginManagerCore.loadDescriptors(deployUnzip.getAbsolutePath(), pluginDescriptors, null, 1);
		if(pluginDescriptors.size() != 1)
		{
			return;
		}

		IdeaPluginDescriptorImpl ideaPluginDescriptor = pluginDescriptors.get(0);

	}
}
