package consulo.webService.update.servlet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

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
import consulo.webService.util.GsonUtil;

/**
 * @author VISTALL
 * @since 30-Aug-16
 */
@WebServlet(urlPatterns = {"/v2/plugins/list"})
public class PluginsListServlet extends HttpServlet
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

			RootService rootService = RootService.getInstance();

			PluginChannelService channelService = rootService.getUpdateService(channel);

			PluginNode[] select = channelService.select(platformVersion);

			String json = GsonUtil.get().toJson(select);

			byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
			resp.setHeader("Content-Type", "application/json");
			resp.setHeader("Content-Lenght", String.valueOf(bytes.length));

			try(OutputStream stream = resp.getOutputStream())
			{
				ByteStreams.copy(new ByteArrayInputStream(bytes), stream);
			}
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
}
