package consulo.webService.update.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import consulo.webService.RootService;
import consulo.webService.ServiceIsNotReadyException;
import consulo.webService.update.PluginChannelService;
import consulo.webService.update.UpdateChannel;

/**
 * @author VISTALL
 * @since 30-Aug-16
 */
@WebServlet(urlPatterns = {"/v2/plugins/list"})
public class PluginsListServlet extends HttpServlet
{
	private static final Logger LOGGER = LoggerFactory.getLogger(PluginsListServlet.class);

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

			RootService rootService = RootService.getInstance();

			PluginChannelService channelService = rootService.getUpdateService(channel);


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
