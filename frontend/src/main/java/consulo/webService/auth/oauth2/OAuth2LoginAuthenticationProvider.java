package consulo.webService.auth.oauth2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Component;

/**
 * @author VISTALL
 * @since 20/08/2021
 */
@Component
public class OAuth2LoginAuthenticationProvider implements AuthenticationProvider
{
	@Autowired
	private UserDetailsService myUserDetailsService;

	@Autowired
	private TokenStore myTokenStore;

	public OAuth2LoginAuthenticationProvider(UserDetailsService userDetailsService, TokenStore tokenStore)
	{
		myUserDetailsService = userDetailsService;
		myTokenStore = tokenStore;
	}

	@Override
	public Authentication authenticate(Authentication temp) throws AuthenticationException
	{
		OAuth2Authentication authentication = (OAuth2Authentication) temp;

		Authentication userAuthentication = authentication.getUserAuthentication();
		if(!(userAuthentication instanceof UsernamePasswordAuthenticationToken))
		{
			throw new BadCredentialsException("wrong auth");
		}

		return authentication;
	}

	@Override
	public boolean supports(Class<?> aClass)
	{
		return aClass == OAuth2Authentication.class;
	}
}
