package org.mustbe.consulo.war.servlet;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.mustbe.consulo.war.SystemAvailable;
import org.mustbe.consulo.war.ide.IdeDirManager;
import org.mustbe.consulo.war.ide.IdeManager;
import org.mustbe.consulo.war.plugins.PluginManagerNew;
import com.intellij.util.ArrayUtil;

/**
 * @author VISTALL
 * @since 22.04.14
 */
public class DownloadServlet extends HttpServlet
{
	private static final String[] OSes = new String[] {"win", "mac", "linux"};

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		if(!SystemAvailable.INSTANCE.get())
		{
			resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
			return;
		}

		String osValue = req.getParameter("os");
		if(!ArrayUtil.contains(osValue, OSes))
		{
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		int buildValue = PluginManagerNew.toBuild(req.getParameter("build"));

		IdeDirManager byBuild = IdeManager.INSTANCE.findByBuild(buildValue);

		File downloadFile = byBuild.getDownloadFile(osValue);
		if(!downloadFile.exists())
		{
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		resp.setContentLength((int) downloadFile.length());
		resp.setContentType("application/octet-stream");
		resp.setHeader("Content-Disposition", "filename=\"" + downloadFile.getName() + "\"");
		FileUtils.copyFile(downloadFile, resp.getOutputStream());
	}
}
