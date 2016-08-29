package org.mustbe.consulo.war.servlet;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.mustbe.consulo.war.model.PluginInfo;
import org.mustbe.consulo.war.plugins.PluginDirManager;
import org.mustbe.consulo.war.plugins.PluginManagerNew;
import org.mustbe.consulo.war.util.HibernateUtil;
import com.google.common.io.Files;

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

		int buildValue = PluginManagerNew.toBuild(req.getParameter("build"));

		String uuidValue = req.getParameter("uuid");
		if(uuidValue == null)
		{
			uuidValue = String.valueOf(System.currentTimeMillis());
		}

		PluginDirManager pluginDir = PluginManagerNew.INSTANCE.findByBuild(buildValue);

		File file = pluginDir.getPlugin(idValue);
		if(!file.exists())
		{
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		resp.setContentLength((int) file.length());
		resp.setContentType("application/octet-stream");
		resp.setHeader("Content-Disposition", "filename=\"" + file.getName() + "\"");
		Files.copy(file, resp.getOutputStream());

		increaseDownloadCount(idValue);
	}

	private static void increaseDownloadCount(String id)
	{
		Session session = HibernateUtil.getSessionFactory().openSession();
		try
		{
			Transaction tx = session.beginTransaction();

			PluginInfo pluginInfo = (PluginInfo) session.get(PluginInfo.class, id);
			if(pluginInfo == null)
			{
				pluginInfo = new PluginInfo(id);
			}

			pluginInfo.downloadCount ++;

			session.persist(pluginInfo);

			tx.commit();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			session.close();
		}
	}
}
