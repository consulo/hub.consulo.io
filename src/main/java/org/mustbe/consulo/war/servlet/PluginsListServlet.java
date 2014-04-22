package org.mustbe.consulo.war.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mustbe.consulo.war.PluginDirManager;
import org.mustbe.consulo.war.PluginManagerNew;

/**
 * @author VISTALL
 * @since 21.04.14
 */
public class PluginsListServlet extends HttpServlet
{
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException
	{
		int buildValue = PluginManagerNew.toBuild(req.getParameter("build"));

		response.setContentType("text/xml");

		PluginDirManager pluginDir = PluginManagerNew.INSTANCE.findPluginDir(buildValue);

		PrintWriter writer = response.getWriter();
		writer.write(pluginDir.getXmlListText());
		writer.close();
	}
}
