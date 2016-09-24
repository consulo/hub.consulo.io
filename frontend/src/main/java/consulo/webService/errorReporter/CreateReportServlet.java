package consulo.webService.errorReporter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.intellij.openapi.util.io.StreamUtil;

/**
 * @author VISTALL
 * @since 24-Sep-16
 */
@WebServlet(urlPatterns = {"/v2/consulo/errorReporter/create"})
public class CreateReportServlet extends HttpServlet
{
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		resp.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE);
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try(InputStream inputStream = req.getInputStream())
		{
			StreamUtil.copyStreamContent(inputStream, outputStream);
		}

		String text = outputStream.toString("UTF-8");
		PrintWriter writer = resp.getWriter();
		writer.print("unauthorized");
		writer.close();
	}
}
