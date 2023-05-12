package consulo.procoeton.core.auth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * @author VISTALL
 * @since 11/05/2023
 */
public class VaadinAuthenticationManager implements AuthenticationManager
{
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException
	{
		return authentication;
	}
}
