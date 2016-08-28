package consulo.webService.update.servlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import com.intellij.openapi.util.io.FileUtil;
import consulo.webService.RootController;
import consulo.webService.ServiceIsNotReadyException;
import consulo.webService.update.UpdateChannel;
import consulo.webService.update.UpdateService;

/**
 * @author VISTALL
 * @since 28-Aug-16
 */
public class PluginsDeployServlet extends HttpServlet
{
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

			UpdateService updateService = RootController.getInstance().getUpdateService();

			File tempFile = updateService.createTempFile("deploy", "zip");

			try (ServletInputStream inputStream = req.getInputStream())
			{
				try (OutputStream output = new FileOutputStream(tempFile))
				{
					IOUtils.copy(inputStream, output);
				}
			}

			FileUtil.asyncDelete(tempFile);
		}
		catch(ServiceIsNotReadyException e)
		{
			resp.sendRedirect("/status");
		}
	}
}
