package org.mustbe.consulo.war.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.war.plugins.PluginManagerNew;
import com.intellij.openapi.util.JDOMUtil;

/**
 * @author VISTALL
 * @since 12.12.14
 */
public class UpdateListServlet extends HttpServlet
{
	public static class ProjectInfo
	{
		private int myBuildNumber;
		private long myTimeMillis;

		public ProjectInfo(int buildNumber, long timeMillis)
		{
			myBuildNumber = buildNumber;
			myTimeMillis = timeMillis;
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException
	{
		int buildValue = PluginManagerNew.toBuild(req.getParameter("build"));

		response.setContentType("text/xml");


		String text = "<product name=\"Consulo\"></product>";
		if(buildValue != Integer.MAX_VALUE)
		{
			ProjectInfo projectInfo = getBuildNumberFromWebServer();
			if(projectInfo.myBuildNumber != buildValue)
			{
				Element element = new Element("product");
				element.setAttribute("name", "Consulo");

				Element channelElement = new Element("channel");
				element.addContent(channelElement);
				channelElement.setAttribute("id", "CONSULO1EAP");
				channelElement.setAttribute("name", "Consulo 1 EAP");
				channelElement.setAttribute("status", "eap");
				channelElement.setAttribute("url", "https://github.com/consulo/consulo/wiki/Downloads");
				channelElement.setAttribute("feedback", "https://github.com/consulo/consulo/issues");
				channelElement.setAttribute("majorVersion", "1");

				Element buildElement = new Element("build");
				channelElement.addContent(buildElement);
				buildElement.setAttribute("number", String.valueOf(projectInfo.myBuildNumber));
				buildElement.setAttribute("version", "1.0");
				buildElement.setAttribute("releaseDate", new SimpleDateFormat("yyyyMMdd").format(projectInfo.myTimeMillis * 1000L));

				buildElement.addContent(new Element("message").setText("New Consulo build is available"));
				buildElement.addContent(new Element("button").setAttribute("name", "Download").setAttribute("url",
						"https://github.com/consulo/consulo/wiki/Download-links").setAttribute("download", "true"));

				Document document = new Document(element);
				text = JDOMUtil.writeDocument(document, "\n");
			}
		}

		PrintWriter writer = response.getWriter();
		writer.write(text);
		writer.close();
	}

	@NotNull
	private static ProjectInfo getBuildNumberFromWebServer()
	{
		try
		{
			URL projectsUrl = new URL("http://must-be.org/vulcan/projects.jsp");
			Document document = JDOMUtil.loadDocument(projectsUrl);

			for(Element element : document.getRootElement().getChildren("project"))
			{
				String name = element.getAttributeValue("name");
				if("consulo".equals(name))
				{
					String statusText = element.getChildText("status");
					if(!"PASS".equalsIgnoreCase(statusText))
					{
						return new ProjectInfo(Integer.MAX_VALUE, System.currentTimeMillis());
					}

					Element timestamp = element.getChild("timestamp");

					String timeMillis = "0";
					if(timestamp != null)
					{
						timeMillis = timestamp.getAttributeValue("millis", "0");
					}
					String buildNumberText = element.getChildText("build-number");
					return new ProjectInfo(Integer.parseInt(buildNumberText), Long.parseLong(timeMillis));
				}
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		catch(JDOMException e)
		{
			e.printStackTrace();
		}
		return new ProjectInfo(Integer.MAX_VALUE, System.currentTimeMillis());
	}
}
