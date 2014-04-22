package org.mustbe.consulo.war.servlet.admin;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author VISTALL
 * @since 22.04.14
 */
public class AdminMakeReleaseServlet extends HttpServlet
{
	public AdminMakeReleaseServlet()
	{

	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		resp.getWriter().println("Hello World");
	}
}
