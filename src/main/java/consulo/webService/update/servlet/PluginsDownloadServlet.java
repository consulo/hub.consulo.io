package consulo.webService.update.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.io.ByteStreams;
import com.intellij.openapi.diagnostic.Logger;
import consulo.webService.RootService;
import consulo.webService.ServiceIsNotReadyException;
import consulo.webService.update.PluginChannelService;
import consulo.webService.update.PluginNode;
import consulo.webService.update.UpdateChannel;

/**
 * @author VISTALL
 * @since 30-Aug-16
 */
@WebServlet(urlPatterns = {"/api/v2/consulo/plugins/download"})
public class PluginsDownloadServlet extends HttpServlet
{
	private static final Logger LOGGER = Logger.getInstance(PluginsListServlet.class);

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
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

			String platformVersion = req.getParameter("platformVersion");
			if(platformVersion == null)
			{
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}

			String pluginId = req.getParameter("pluginId");
			if(pluginId == null)
			{
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}

			RootService rootService = RootService.getInstance();

			PluginChannelService channelService = rootService.getUpdateService(channel);

			PluginNode select = channelService.select(platformVersion, pluginId);
			if(select == null)
			{
				resp.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}

			File targetFile = select.targetFile;
			assert targetFile != null;

			resp.setContentLength((int) targetFile.length());
			resp.setContentType("application/octet-stream");
			resp.setHeader("Content-Disposition", "filename=\"" + targetFile.getName() + "\"");

			try (FileInputStream fileInputStream = new FileInputStream(targetFile))
			{
				try (OutputStream stream = resp.getOutputStream())
				{
					ByteStreams.copy(fileInputStream, stream);
				}
			}
		}
		catch(ServiceIsNotReadyException e)
		{
			resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
		}
		catch(Exception e)
		{
			LOGGER.error(e.getMessage(), e);

			resp.sendError(HttpServletResponse.SC_FORBIDDEN);
		}
	}
}
