package consulo.procoeton.core.backend;

import com.fasterxml.jackson.core.type.TypeReference;
import consulo.procoeton.core.ProPropertiesService;
import org.apache.http.client.methods.HttpRequestBase;

/**
 * @author VISTALL
 * @since 04/05/2023
 */
public interface BackendRequestTarget<T>
{
	T getDefaultValue();

	TypeReference<T> getType();

	String getHost(ProPropertiesService propertiesService);

	String buildUrl(String hostName);

	default String getMethod()
	{
		return "GET";
	}

	default void addHeaders(ProPropertiesService propertiesService, HttpRequestBase requst)
	{
	}
}
