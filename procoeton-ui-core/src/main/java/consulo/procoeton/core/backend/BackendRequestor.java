package consulo.procoeton.core.backend;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import consulo.hub.shared.ServiceAccounts;
import consulo.procoeton.core.ProPropertiesService;
import consulo.procoeton.core.auth.backend.BackendAuthenticationToken;
import consulo.procoeton.core.util.PropertySet;
import jakarta.annotation.Nullable;
import jakarta.annotation.PreDestroy;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

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
public abstract class BackendRequestor
{
	@Autowired
	private ObjectMapper myObjectMapper;

	@Autowired
	private ProPropertiesService myPropertiesService;

	private CloseableHttpClient myClient = HttpClients.custom().setDefaultRequestConfig(RequestConfig.custom().setConnectTimeout(5000).setSocketTimeout(5000).build()).build();

	private final String myHostKey;
	private final String myPasswordKey;

	protected BackendRequestor(String hostKey, String passwordKey)
	{
		myHostKey = hostKey;
		myPasswordKey = passwordKey;
	}

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
			host = propertySet.getStringProperty(myHostKey);
			key = propertySet.getStringProperty(myPasswordKey);
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
			builder.addHeader(HttpHeaders.AUTHORIZATION, "Basic " + basicAuth);
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
