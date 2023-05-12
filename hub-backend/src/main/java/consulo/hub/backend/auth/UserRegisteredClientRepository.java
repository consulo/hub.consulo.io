package consulo.hub.backend.auth;

import consulo.hub.shared.auth.domain.UserAccount;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * @author VISTALL
 * @since 04/05/2023
 */
@Service
public class UserRegisteredClientRepository implements RegisteredClientRepository
{
	private final UserAccountService myUserAccountService;

	public UserRegisteredClientRepository(UserAccountService userAccountService)
	{
		myUserAccountService = userAccountService;
	}

	@Override
	public void save(RegisteredClient registeredClient)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public RegisteredClient findById(String id)
	{
		return mapAccount(myUserAccountService.findUser(Long.parseLong(id)));
	}

	@Override
	public RegisteredClient findByClientId(String clientId)
	{
		return mapAccount(myUserAccountService.findUser(clientId));
	}

	private RegisteredClient mapAccount(UserAccount user)
	{
		if(user == null)
		{
			return null;
		}

		return RegisteredClient.withId(String.valueOf(user.getId()))
				.clientId(user.getUsername())
				.clientSecret(user.getPassword())
				.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
				.authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
				.authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
				.tokenSettings(TokenSettings.builder().accessTokenTimeToLive(Duration.ofDays(120)).build())
				.clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
				.build();
	}
}
