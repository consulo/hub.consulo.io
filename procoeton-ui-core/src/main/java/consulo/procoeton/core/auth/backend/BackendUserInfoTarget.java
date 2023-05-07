package consulo.procoeton.core.auth.backend;

import com.fasterxml.jackson.core.type.TypeReference;
import consulo.hub.shared.auth.domain.UserAccount;
import consulo.procoeton.core.ProPropertiesService;
import consulo.procoeton.core.backend.ApiBackendKeys;
import consulo.procoeton.core.backend.BackendRequestTarget;

/**
 * @author VISTALL
 * @since 04/05/2023
 */
public class BackendUserInfoTarget implements BackendRequestTarget<UserAccount>
{
	public static final BackendUserInfoTarget INSTANCE = new BackendUserInfoTarget();

	@Override
	public TypeReference<UserAccount> getType()
	{
		return new TypeReference<UserAccount>()
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
		return hostName + "/api/user/info";
	}

	@Override
	public UserAccount getDefaultValue()
	{
		return null;
	}

	@Override
	public String getMethod()
	{
		return "GET";
	}
}
