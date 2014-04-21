package org.mustbe.consulo.war.servlet;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.mustbe.consulo.war.util.ApplicationConfiguration;

/**
 * @author VISTALL
 * @since 21.04.14
 */
public class PluginsDownloadServlet extends HttpServlet
{
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		String idValue = req.getParameter("id");
		if(idValue == null)
		{
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		String buildValue = req.getParameter("build");
		if(buildValue == null)
		{
			buildValue = PluginsConstants.SNAPSHOT;
		}

		String uuidValue = req.getParameter("uuid");
		if(uuidValue == null)
		{
			uuidValue = String.valueOf(System.currentTimeMillis());
		}

		String property = ApplicationConfiguration.getProperty("consulo.plugins.work.dir");

		File file = new File(property, idValue + ".zip");
		if(!file.exists())
		{
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		resp.setContentType("application/octet-stream");
		resp.setHeader("Content-Disposition", "filename=\"" + file.getName() + "\"");
		FileUtils.copyFile(file, resp.getOutputStream());
	}
}
