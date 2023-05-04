package consulo.procoeton.core.backend;

import org.springframework.stereotype.Service;

/**
 * @author VISTALL
 * @since 22/08/2021
 */
@Service
public class ApiBackendRequestor extends BackendRequestor
{
	public ApiBackendRequestor()
	{
		super(ApiBackendKeys.BACKEND_HOST_URL_KEY, ApiBackendKeys.BACKEND_HOST_PASSWORD);
	}
}
