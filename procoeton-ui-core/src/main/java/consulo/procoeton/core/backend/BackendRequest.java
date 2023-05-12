package consulo.procoeton.core.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.QueryParameters;
import consulo.procoeton.core.ProPropertiesService;
import consulo.procoeton.core.service.LogoutService;
import consulo.procoeton.core.vaadin.util.Notifications;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.http.HttpConnectTimeoutException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * @author VISTALL
 * @since 04/05/2023
 */
public class BackendRequest<T>
{
	private final BackendRequestTarget<T> myTarget;
	private final CloseableHttpClient myClient;
	private final ObjectMapper myObjectMapper;
	private final ProPropertiesService myPropertiesService;
	private final LogoutService myLogoutService;

	private final Map<String, String> myHeaders = new LinkedHashMap<>();
	private final Map<String, String> myParameters = new LinkedHashMap<>();

	protected BackendRequest(BackendRequestTarget<T> target, CloseableHttpClient client, ObjectMapper objectMapper, ProPropertiesService propertiesService, LogoutService logoutService)
	{
		myTarget = target;
		myClient = client;
		myObjectMapper = objectMapper;
		myPropertiesService = propertiesService;
		myLogoutService = logoutService;
	}

	public BackendRequest authorizationHeader(String value)
	{
		return header("Authorization", value);
	}

	public BackendRequest header(String key, String value)
	{
		myHeaders.put(key, value);
		return this;
	}

	public BackendRequest parameter(String key, String value)
	{
		myParameters.put(key, value);
		return this;
	}

	public void execute(UI ui, BiConsumer<UI, T> consumer)
	{
		try
		{
			T value = executeImpl();
			consumer.accept(ui, value);
		}
		catch(BackendServiceDownException e)
		{
			if(!ui.isClosing())
			{
				ui.access(() -> Notifications.error("Server Busy. Try Again Later"));
			}
		}
	}

	@Deprecated
	public T execute()
	{
		return executeImpl();
	}

	private T executeImpl()
	{
		String host = myTarget.getHost(myPropertiesService);

		HttpRequestBase request;
		switch(myTarget.getMethod())
		{
			case "GET":
				request = new HttpGet(myTarget.buildUrl(host) + (myParameters.isEmpty() ? "" : "?" + QueryParameters.simple(myParameters).getQueryString()));
				break;
			case "POST":
				request = new HttpPost(myTarget.buildUrl(host) + (myParameters.isEmpty() ? "" : "?" + QueryParameters.simple(myParameters).getQueryString()));
				break;
			default:
				throw new UnsupportedOperationException(myTarget.getMethod());
		}

		request.addHeader("Content-Type", "application/json");

		for(Map.Entry<String, String> entry : myHeaders.entrySet())
		{
			request.addHeader(entry.getKey(), entry.getValue());
		}

		myTarget.addHeaders(myPropertiesService, request);

		try
		{
			T value = myClient.execute(request, response ->
			{
				int statusCode = response.getStatusLine().getStatusCode();
				if(statusCode == 401 || statusCode == 403)
				{
					myLogoutService.logout(UI.getCurrent(), false);
				}

				if(statusCode != 200)
				{
					throw new IOException("request failed. Code: " + statusCode);
				}

				String json = EntityUtils.toString(response.getEntity());
				return myObjectMapper.readValue(json, myTarget.getType());
			});
			return value == null ? myTarget.getDefaultValue() : value;
		}
		catch(HttpHostConnectException | HttpConnectTimeoutException e)
		{
			throw new BackendServiceDownException(e);
		}
		catch(IOException ignored)
		{
			return myTarget.getDefaultValue();
		}
	}
}
