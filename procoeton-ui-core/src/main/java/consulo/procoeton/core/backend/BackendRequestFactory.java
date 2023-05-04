package consulo.procoeton.core.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import consulo.procoeton.core.ProPropertiesService;
import jakarta.annotation.PreDestroy;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @author VISTALL
 * @since 04/05/2023
 */
@Service
public class BackendRequestFactory
{
	private CloseableHttpClient myClient = HttpClients.custom().setDefaultRequestConfig(RequestConfig.custom().setConnectTimeout(5000).setSocketTimeout(5000).build()).build();

	private final ObjectMapper myObjectMapper;

	private final ProPropertiesService myPropertiesService;

	@Autowired
	public BackendRequestFactory(ObjectMapper objectMapper, ProPropertiesService propertiesService)
	{
		myObjectMapper = objectMapper;
		myPropertiesService = propertiesService;
	}

	public <T> BackendRequest<T> newRequest(BackendRequestTarget<T> target)
	{
		return new BackendRequest<>(target, myClient, myObjectMapper, myPropertiesService);
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
