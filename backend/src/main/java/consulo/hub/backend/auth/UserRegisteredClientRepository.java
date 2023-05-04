package consulo.hub.backend.auth;

import consulo.hub.shared.auth.domain.UserAccount;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.stereotype.Service;

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
		throw new UnsupportedOperationException();
	}

	@Override
	public RegisteredClient findByClientId(String clientId)
	{
		UserAccount user = myUserAccountService.findUser(clientId);
		if(user == null)
		{
			return null;
		}

		return RegisteredClient.withId(String.valueOf(user.getId()))
				.clientId(user.getUsername())
				.clientSecret(user.getPassword())
				.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
				.authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
				.authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
				.redirectUri("http://127.0.0.1:22333/login/oauth2/code/messaging-client-oidc")
				.redirectUri("http://127.0.0.1:22333/authorized")
				.scope(OidcScopes.PROFILE)
				.scope(OidcScopes.EMAIL)
				.scope("message.read")
				.scope("message.write")
				.clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
				.build();
	}
}
