package consulo.procoeton.core.auth.backend;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * @author VISTALL
 * @since 04/09/2021
 */
public class BackendAuthenticationToken extends UsernamePasswordAuthenticationToken
{
	private final String myToken;

	public BackendAuthenticationToken(Object principal, String token, Collection<? extends GrantedAuthority> authorities)
	{
		super(principal, "N/A", authorities);
		myToken = token;
	}

	public String getToken()
	{
		return myToken;
	}
}
