package consulo.hub.frontend.backend;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import consulo.hub.frontend.PropertiesService;
import consulo.hub.frontend.util.PropertyKeys;
import consulo.hub.frontend.util.PropertySet;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.lang.reflect.Type;
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

	@Autowired
	private PropertiesService myPropertiesService;

	private CloseableHttpClient myClient = HttpClients.custom().setDefaultRequestConfig(RequestConfig.custom().setConnectTimeout(5000).setSocketTimeout(5000).build()).build();

	public <T> T runRequest(String urlSuffix, Map<String, String> parameters, TypeReference<T> valueClazz) throws Exception
	{
		String host = "http://localhost:2233";
		String key = null;

		if(!myPropertiesService.isNotInstalled())
		{
			PropertySet propertySet = myPropertiesService.getPropertySet();
			host = propertySet.getStringProperty(PropertyKeys.BACKEND_HOST_URL_KEY);
			key = propertySet.getStringProperty(PropertyKeys.BACKEND_HOST_OAUTH_KEY);
		}

		return runRequest(host, key, urlSuffix, parameters, valueClazz);
	}

	public <T> T runRequest(String urlSuffix, Map<String, String> parameters, Class<T> valueClazz) throws Exception
	{
		return runRequest(urlSuffix, parameters, new TypeReference<T>()
		{
			@Override
			public Type getType()
			{
				return valueClazz;
			}
		});
	}

	public <T> T runRequest(String host, String key, String urlSuffix, Map<String, String> parameters, TypeReference<T> valueClazz) throws Exception
	{
		RequestBuilder builder = RequestBuilder.get(host + "/api/private" + urlSuffix);
		for(Map.Entry<String, String> entry : parameters.entrySet())
		{
			builder.addParameter(entry.getKey(), entry.getValue());
		}

		if(key != null)
		{
			builder.addHeader("Authorization", "Bearer " + key);
		}

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if(authentication instanceof BackendAuthenticationToken)
		{
			builder.addHeader("UserAuthorization", ((BackendAuthenticationToken) authentication).getToken());
		}

		return myClient.execute(builder.build(), response ->
		{
			if(response.getStatusLine().getStatusCode() != 200)
			{
				throw new IOException("request failed. Code: " + response.getStatusLine().getStatusCode());
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
