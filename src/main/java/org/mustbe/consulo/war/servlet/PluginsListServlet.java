package org.mustbe.consulo.war.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mustbe.consulo.war.plugins.PluginDirManager;
import org.mustbe.consulo.war.plugins.PluginManagerNew;
import org.mustbe.consulo.war.SystemAvailable;

/**
 * @author VISTALL
 * @since 21.04.14
 */
public class PluginsListServlet extends HttpServlet
{
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException
	{
		if(!SystemAvailable.INSTANCE.get())
		{
			response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
			return;
		}

		int buildValue = PluginManagerNew.toBuild(req.getParameter("build"));

		response.setContentType("text/xml");

		PluginDirManager pluginDir = PluginManagerNew.INSTANCE.findByBuild(buildValue);

		PrintWriter writer = response.getWriter();
		writer.write(pluginDir.getXmlListText());
		writer.close();
	}
}
