package consulo.webService.servlet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.io.ByteStreams;
import consulo.webService.ChildService;
import consulo.webService.RootService;
import consulo.webService.util.GsonUtil;

/**
 * @author VISTALL
 * @since 28-Aug-16
 */
@WebServlet(urlPatterns = {"/api/v2/status"})
public class StatusServlet extends HttpServlet
{
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException
	{
		Map<String, Boolean> map = new LinkedHashMap<>();
		for(ChildService childService : RootService.getInstanceNoState().getChildServices())
		{
			map.put(childService.getTitle(), childService.isInitialized());
		}

		String json = GsonUtil.get().toJson(map);

		byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
		response.setHeader("Content-Type", "application/json");
		response.setHeader("Content-Length", String.valueOf(bytes.length));

		try (OutputStream stream = response.getOutputStream())
		{
			ByteStreams.copy(new ByteArrayInputStream(bytes), stream);
		}
	}
}
