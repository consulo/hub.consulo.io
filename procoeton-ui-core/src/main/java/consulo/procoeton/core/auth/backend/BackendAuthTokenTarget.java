package consulo.procoeton.core.auth.backend;

import com.fasterxml.jackson.core.type.TypeReference;
import consulo.procoeton.core.ProPropertiesService;
import consulo.procoeton.core.backend.ApiBackendKeys;
import consulo.procoeton.core.backend.BackendRequestTarget;

import java.util.Map;

/**
 * @author VISTALL
 * @since 04/05/2023
 */
public class BackendAuthTokenTarget implements BackendRequestTarget<Map<String, Object>>
{
	public static final BackendAuthTokenTarget INSTANCE = new BackendAuthTokenTarget();

	@Override
	public TypeReference<Map<String, Object>> getType()
	{
		return new TypeReference<Map<String, Object>>()
		{
		};
	}

	@Override
	public String getHost(ProPropertiesService propertiesService)
	{
		return propertiesService.getPropertySet().getStringProperty(ApiBackendKeys.BACKEND_HOST_URL_KEY);
	}

	@Override
	public String buildUrl(String hostName)
	{
		return hostName + "/api/oauth2/token";
	}

	@Override
	public Map<String, Object> getDefaultValue()
	{
		return null;
	}

	@Override
	public String getMethod()
	{
		return "POST";
	}
}
