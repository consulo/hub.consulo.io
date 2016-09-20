package consulo.webService.update.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.intellij.openapi.diagnostic.Logger;
import consulo.webService.ServiceIsNotReadyException;
import consulo.webService.update.UpdateChannel;

/**
 * @author VISTALL
 * @since 28-Aug-16
 */
@WebServlet(urlPatterns = {"/v2/consulo/plugins/deploy"})
public class PluginsDeployServlet extends HttpServlet
{
	private static final Logger LOGGER = Logger.getInstance(PluginsDeployServlet.class);

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

			PluginDeploy.deployPlugin(channel, req::getInputStream);
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
