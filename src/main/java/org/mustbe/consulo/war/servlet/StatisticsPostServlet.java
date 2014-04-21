package org.mustbe.consulo.war.servlet;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.mustbe.consulo.war.model.StatisticEntry;
import org.mustbe.consulo.war.util.HibernateUtil;

/**
 * @author VISTALL
 * @since 21.04.14
 */
public class StatisticsPostServlet extends HttpServlet
{
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		String uuidValue = req.getParameter("uuid");
		if(uuidValue == null)
		{
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		String contentValue = req.getParameter("content");
		if(contentValue == null)
		{
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		Map<String, Long> map = toData(contentValue);
		if(map.isEmpty())
		{
			return;
		}

		Session session = HibernateUtil.getSessionFactory().openSession();
		try
		{
			session.beginTransaction();

			UUID uuid = UUID.fromString(uuidValue);
			StatisticEntry load = (StatisticEntry) session.get(StatisticEntry.class, uuid);
			if(load == null)
			{
				load = new StatisticEntry(uuid);
			}

			load.incData(map);

			session.persist(load);

			session.getTransaction().commit();
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

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		resp.sendError(HttpServletResponse.SC_NOT_FOUND);
	}

	public static Map<String, Long> toData(String content)
	{
		try
		{
			Map<String, Long> map = new LinkedHashMap<String, Long>();

			String[] lines = content.split(";");

			for(String line : lines)
			{
				int i = line.indexOf(":");

				String key = line.substring(0, i);


				String datas = line.substring(i + 1, line.length());

				String[] keyAndCounts = datas.split(",");
				for(String keyAndCount : keyAndCounts)
				{
					String[] temp = keyAndCount.split("=");

					Long value = Long.valueOf(temp[1]);
					if(value <= 0)
					{
						continue;
					}

					String keyToPut = key + "@" + temp[0];
					keyToPut = keyToPut.trim();
					map.put(keyToPut, value);
				}
			}

			return map;
		}
		catch(Exception e)
		{
			return Collections.emptyMap();
		}
	}
}
