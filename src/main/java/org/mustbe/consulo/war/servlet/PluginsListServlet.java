package org.mustbe.consulo.war.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author VISTALL
 * @since 21.04.14
 */
public class PluginsListServlet extends HttpServlet
{
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException
	{
		String buildValue = req.getParameter("build");
		if(buildValue == null)
		{
			buildValue = PluginsConstants.SNAPSHOT;
		}
		response.setContentType("text/xml");

		PrintWriter writer = response.getWriter();
		writer.write(PluginManager.INSTANCE.getXmlRepoText());
		writer.close();
	}
}
