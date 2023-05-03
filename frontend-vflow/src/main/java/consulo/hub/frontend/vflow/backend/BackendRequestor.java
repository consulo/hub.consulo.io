package consulo.hub.frontend.vflow.backend;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import consulo.hub.frontend.vflow.PropertiesService;
import consulo.hub.frontend.vflow.util.PropertyKeys;
import consulo.hub.frontend.vflow.util.PropertySet;
import consulo.hub.shared.ServiceAccounts;
import jakarta.annotation.Nullable;
import jakarta.annotation.PreDestroy;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.function.Supplier;

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

	@Nullable
	public <T> T runRequest(String urlSuffix, Map<String, String> parameters, TypeReference<T> valueClazz) throws Exception
	{
		return runRequest(urlSuffix, parameters, valueClazz, () -> null);
	}

	@Nullable
	public <T> T runRequest(String urlSuffix, Map<String, String> parameters, TypeReference<T> valueClazz, Supplier<T> defaultValueGetter) throws Exception
	{
		String host = "http://localhost:2233";
		String key = null;

		if(myPropertiesService.isInstalled())
		{
			PropertySet propertySet = myPropertiesService.getPropertySet();
			host = propertySet.getStringProperty(PropertyKeys.BACKEND_HOST_URL_KEY);
			key = propertySet.getStringProperty(PropertyKeys.BACKEND_HOST_PASSWORD);
		}

		return runRequest(host, key, urlSuffix, parameters, valueClazz, defaultValueGetter);
	}

	public <T> T runRequest(String urlSuffix, Map<String, String> parameters, Class<T> valueClazz) throws Exception
	{
		return runRequest(urlSuffix, parameters, valueClazz, () -> null);
	}

	public <T> T runRequest(String urlSuffix, Map<String, String> parameters, Class<T> valueClazz, Supplier<T> defaultValueGetter) throws Exception
	{
		return runRequest(urlSuffix, parameters, new TypeReference<T>()
		{
			@Override
			public Type getType()
			{
				return valueClazz;
			}
		}, defaultValueGetter);
	}

	public <T> T runRequest(String host, String key, String urlSuffix, Map<String, String> parameters, TypeReference<T> valueClazz) throws Exception
	{
		return runRequest(host, key, urlSuffix, parameters, valueClazz, () -> null);
	}

	public <T> T runRequest(String host, String password, String urlSuffix, Map<String, String> parameters, TypeReference<T> valueClazz, Supplier<T> defaultValueGetter) throws Exception
	{
		RequestBuilder builder = RequestBuilder.get(host + "/api/private" + urlSuffix);
		builder.addHeader("Content-Type", "application/json");

		for(Map.Entry<String, String> entry : parameters.entrySet())
		{
			builder.addParameter(entry.getKey(), entry.getValue());
		}

		if(password != null)
		{
			String basicAuth = Base64.getEncoder().encodeToString((ServiceAccounts.HUB + ":" + password).getBytes(StandardCharsets.UTF_8));
			builder.addHeader("Authorization", "Basic " + basicAuth);
		}

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if(authentication instanceof BackendAuthenticationToken)
		{
			builder.addHeader("UserAuthorization", ((BackendAuthenticationToken) authentication).getToken());
		}

		try
		{
			T value = myClient.execute(builder.build(), response ->
			{
				if(response.getStatusLine().getStatusCode() != 200)
				{
					throw new IOException("request failed. Code: " + response.getStatusLine().getStatusCode());
				}

				String json = EntityUtils.toString(response.getEntity());
				return myObjectMapper.readValue(json, valueClazz);
			});
			return value == null ? defaultValueGetter.get() : value;
		}
		catch(IOException ignored)
		{
			return defaultValueGetter.get();
		}
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
