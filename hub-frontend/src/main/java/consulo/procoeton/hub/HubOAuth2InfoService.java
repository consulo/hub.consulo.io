package consulo.procoeton.hub;

import consulo.procoeton.core.OAuth2InfoService;
import org.springframework.stereotype.Service;

/**
 * @author VISTALL
 * @since 09/05/2023
 */
@Service
public class HubOAuth2InfoService implements OAuth2InfoService
{
	@Override
	public String getClientName()
	{
		return "Hub";
	}
}
