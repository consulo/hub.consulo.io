package consulo.procoeton.core.auth.backend;

import com.fasterxml.jackson.core.type.TypeReference;
import consulo.hub.shared.ServiceAccounts;
import consulo.hub.shared.ServicesHeaders;
import consulo.hub.shared.auth.domain.UserAccount;
import consulo.procoeton.core.ProPropertiesService;
import consulo.procoeton.core.backend.ApiBackendKeys;
import consulo.procoeton.core.backend.BackendRequestTarget;
import consulo.procoeton.core.util.PropertySet;
import org.apache.http.client.methods.HttpRequestBase;
import org.springframework.http.HttpHeaders;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @author VISTALL
 * @since 05/05/2023
 */
public class BackendUserRegisterTarget implements BackendRequestTarget<UserAccount>
{
	public static final BackendUserRegisterTarget INSTANCE = new BackendUserRegisterTarget();

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
		return hostName + "/api/private/user/register";
	}

	@Override
	public UserAccount getDefaultValue()
	{
		return null;
	}

	@Override
	public void addHeaders(ProPropertiesService propertiesService, HttpRequestBase requst)
	{
		if(!propertiesService.isInstalled())
		{
			return;
		}

		String email = ServiceAccounts.HUB;
		PropertySet propertySet = propertiesService.getPropertySet();
		String password = propertySet.getStringProperty(ApiBackendKeys.BACKEND_HOST_PASSWORD);

		String headerValue = "Basic " + Base64.getEncoder().encodeToString((email + ":" + password).getBytes(StandardCharsets.UTF_8));
		requst.setHeader(HttpHeaders.AUTHORIZATION, headerValue);

		String backendSecureKey = propertySet.getStringProperty(ApiBackendKeys.BACKEND_SECURE_KEY, "");
		requst.setHeader(ServicesHeaders.BACKEND_SECURE_KEY, backendSecureKey);
	}

	@Override
	public String getMethod()
	{
		return "GET";
	}
}

