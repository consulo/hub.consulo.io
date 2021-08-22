package consulo.hub.frontend.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.Map;

/**
 * @author VISTALL
 * @since 22/08/2021
 */
@Service
public class BackendRequestor
{
	@Autowired
	private ObjectMapper myObjectMapper;

	private CloseableHttpClient myClient = HttpClients.custom().setDefaultRequestConfig(RequestConfig.custom().setConnectTimeout(5000).setSocketTimeout(5000).build()).build();

	public <T> T runRequest(String urlSuffix, Map<String, String> parameters, Class<T> valueClazz) throws Exception
	{
		RequestBuilder builder = RequestBuilder.get("http://localhost:22333/private/api" + urlSuffix);
		for(Map.Entry<String, String> entry : parameters.entrySet())
		{
			builder.addParameter(entry.getKey(), entry.getValue());
		}

		builder.addHeader("Authorization", "Bearer 94074cac-9df0-4440-9428-e03f805282f3");

		return myClient.execute(builder.build(), response ->
		{
			if(response.getStatusLine().getStatusCode() != 200)
			{
				throw new IOException("request failed");
			}

			String json = EntityUtils.toString(response.getEntity());
			return myObjectMapper.readValue(json, valueClazz);
		});
	}

	@PreDestroy
	public void destroy()
	{
		try
		{
			myClient.close();
		}
		catch(IOException ignored)
		{
		}
	}
}
