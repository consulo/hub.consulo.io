package consulo.webService.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import consulo.webService.ChildService;
import consulo.webService.RootController;

/**
 * @author VISTALL
 * @since 28-Aug-16
 */
@WebServlet(urlPatterns = {"/status"})
public class StatusServlet extends HttpServlet
{
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException
	{
		response.setContentType("text/html");

		PrintWriter out = response.getWriter();
		out.println("<html>");
		out.println("<head>");
		out.println("<title>Status</title><body>");

		for(ChildService childService : RootController.getInstanceNoState().getChildServices())
		{
			out.append(childService.getTitle()).append(" - <b>").append(childService.isInitialized() ? "Initialized" : "Pending").append("</b><br>");
		}

		out.println("</body></html>");
		out.close();
	}
}
