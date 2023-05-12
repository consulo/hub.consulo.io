package consulo.procoeton.core.auth;

import consulo.hub.shared.auth.domain.UserAccount;
import consulo.procoeton.core.OAuth2InfoService;
import consulo.procoeton.core.auth.backend.BackendAuthenticationToken;
import consulo.procoeton.core.auth.backend.BackendUserInfoTarget;
import consulo.procoeton.core.backend.BackendRequest;
import consulo.procoeton.core.backend.BackendRequestFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationException;

/**
 * @author VISTALL
 * @since 11/05/2023
 */
public class OAuth2AbstractRememberMeServices extends AbstractRememberMeServices
{
	private static class TokenUser extends UserAccount
	{
		private final String myToken;

		private TokenUser(String token, UserAccount userAccount)
		{
			myToken = token;
			setId(userAccount.getId());
			setFirstname(userAccount.getFirstname());
			setLastname(userAccount.getLastname());
			setUsername(userAccount.getUsername());
			setStatus(userAccount.getStatus());
			setRights(userAccount.getRights());
		}
	}

	private final ObjectProvider<BackendRequestFactory> myBackendRequestFactory;

	public OAuth2AbstractRememberMeServices(OAuth2InfoService infoService, ObjectProvider<BackendRequestFactory> backendRequestFactory)
	{
		super(infoService.getClientName(), StubUserDetailsService.INSTANCE);
		myBackendRequestFactory = backendRequestFactory;
		setAlwaysRemember(true);
	}

	@Override
	protected void onLoginSuccess(HttpServletRequest request, HttpServletResponse response, Authentication successfulAuthentication)
	{
		BackendAuthenticationToken token = (BackendAuthenticationToken) successfulAuthentication;

		setCookie(new String[] {token.getName(), token.getToken()}, getTokenValiditySeconds(), request, response);
	}

	@Override
	protected UserDetails processAutoLoginCookie(String[] cookieTokens, HttpServletRequest request, HttpServletResponse response) throws RememberMeAuthenticationException, UsernameNotFoundException
	{
		String accessToken = cookieTokens[1];

		BackendRequest<UserAccount> getAccountRequest = myBackendRequestFactory.getObject().newRequest(BackendUserInfoTarget.INSTANCE);
		getAccountRequest.authorizationHeader("Bearer " + accessToken);

		UserAccount userAccount = getAccountRequest.execute();
		if(userAccount == null)
		{
			throw new UsernameNotFoundException(accessToken);
		}

		return new TokenUser(accessToken, userAccount);
	}

	@Override
	protected Authentication createSuccessfulAuthentication(HttpServletRequest request, UserDetails user)
	{
		TokenUser tokenUser = (TokenUser) user;

		return BackendAuthenticationToken.of(tokenUser, tokenUser.myToken);
	}
}
