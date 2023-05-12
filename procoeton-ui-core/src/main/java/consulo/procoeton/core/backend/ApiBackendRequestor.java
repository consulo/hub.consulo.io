package consulo.procoeton.core.backend;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.UI;
import consulo.hub.shared.ServicesHeaders;
import consulo.procoeton.core.ProPropertiesService;
import consulo.procoeton.core.auth.backend.BackendAuthenticationToken;
import consulo.procoeton.core.service.LogoutService;
import consulo.procoeton.core.util.PropertySet;
import jakarta.annotation.Nullable;
import jakarta.annotation.PreDestroy;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.http.HttpConnectTimeoutException;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author VISTALL
 * @since 22/08/2021
 */
@Service
public class ApiBackendRequestor
{
	private final ObjectMapper myObjectMapper;

	private final ProPropertiesService myPropertiesService;

	private final LogoutService myLogoutService;

	private CloseableHttpClient myClient = HttpClients.custom().setDefaultRequestConfig(RequestConfig.custom().setConnectTimeout(5000).setSocketTimeout(5000).build()).build();

	private final String myHostKey = ApiBackendKeys.BACKEND_HOST_URL_KEY;

	@Autowired
	public ApiBackendRequestor(ObjectMapper objectMapper, ProPropertiesService propertiesService, LogoutService logoutService)
	{
		myObjectMapper = objectMapper;
		myPropertiesService = propertiesService;
		myLogoutService = logoutService;
	}

	@Nullable
	public <T> T runRequest(BackendApiUrl urlSuffix, Map<String, String> parameters, TypeReference<T> valueClazz) throws Exception
	{
		return runRequest(urlSuffix, parameters, valueClazz, () -> null);
	}

	@Nullable
	public <T> T runRequest(BackendApiUrl urlSuffix, Map<String, String> parameters, TypeReference<T> valueClazz, Supplier<T> defaultValueGetter) throws Exception
	{
		String host = "http://localhost:2233";

		if(myPropertiesService.isInstalled())
		{
			PropertySet propertySet = myPropertiesService.getPropertySet();
			host = propertySet.getStringProperty(myHostKey);
		}

		return runRequest(host, urlSuffix, parameters, valueClazz, defaultValueGetter);
	}

	public <T> T runRequest(BackendApiUrl urlSuffix, Map<String, String> parameters, Class<T> valueClazz) throws Exception
	{
		return runRequest(urlSuffix, parameters, valueClazz, () -> null);
	}

	public <T> T runRequest(BackendApiUrl urlSuffix, Map<String, String> parameters, Class<T> valueClazz, Supplier<T> defaultValueGetter) throws Exception
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

	public <T> T runRequest(String host, BackendApiUrl urlSuffix, Map<String, String> parameters, TypeReference<T> valueClazz) throws Exception
	{
		return runRequest(host, urlSuffix, parameters, valueClazz, () -> null);
	}

	public <T> T runRequest(String host, BackendApiUrl url, Map<String, String> parameters, TypeReference<T> valueClazz, Supplier<T> defaultValueGetter) throws Exception
	{
		RequestBuilder builder = RequestBuilder.get(url.build(host));
		builder.addHeader("Content-Type", "application/json");

		for(Map.Entry<String, String> entry : parameters.entrySet())
		{
			builder.addParameter(entry.getKey(), entry.getValue());
		}

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if(authentication instanceof BackendAuthenticationToken)
		{
			builder.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + ((BackendAuthenticationToken) authentication).getToken());
		}

		if(myPropertiesService.isInstalled() && url.isPrivate())
		{
			builder.addHeader(ServicesHeaders.BACKEND_SECURE_KEY, myPropertiesService.getPropertySet().getStringProperty(ApiBackendKeys.BACKEND_SECURE_KEY, ""));
		}

		try
		{
			T value = myClient.execute(builder.build(), response ->
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
				return myObjectMapper.readValue(json, valueClazz);
			});
			return value == null ? defaultValueGetter.get() : value;
		}
		catch(HttpHostConnectException | HttpConnectTimeoutException e)
		{
			throw new BackendServiceDownException(e);
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
