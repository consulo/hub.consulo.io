package consulo.webService.statistics.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author VISTALL
 * @since 21.04.14
 */
@WebServlet(urlPatterns = {"/api/v2/consulo/statistics/show"})
public class StatisticsShowServlet extends HttpServlet
{
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException
	{
		response.setContentType("text/html");

		PrintWriter out = response.getWriter();
		out.println("<html>");
		out.println("<head>");
		out.println("<title>Statistics</title><body>");

		/*Session session = HibernateUtil.getSessionFactory().openSession();
		try
		{
			Transaction tx = session.beginTransaction();

			List list = session.createCriteria(StatisticEntry.class).list();
			tx.commit();

			for(Object o : list)
			{
				StatisticEntry statisticEntry = (StatisticEntry) o;
				out.println("User: " + statisticEntry.getUUID() + "<br>");
				for(Map.Entry<String, Long> entry : statisticEntry.getValues().entrySet())
				{
					out.println("<span>&nbsp;&nbsp;&nbsp;&nbsp;</span>" + entry.getKey() + " = " + entry.getValue() + "<br>");
				}
			}

		}
		finally
		{
			session.close();
		} */
		out.println("</body></html>");
		out.close();
	}
}
