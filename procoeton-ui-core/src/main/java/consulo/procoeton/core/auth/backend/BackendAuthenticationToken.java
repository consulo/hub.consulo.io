package consulo.procoeton.core.auth.backend;

import consulo.hub.shared.auth.domain.UserAccount;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * @author VISTALL
 * @since 04/09/2021
 */
public class BackendAuthenticationToken extends UsernamePasswordAuthenticationToken
{
	public static BackendAuthenticationToken of(UserAccount userAccount, String token)
	{
		return new BackendAuthenticationToken(userAccount, token, userAccount.getAuthorities());
	}

	private final String myToken;

	private BackendAuthenticationToken(UserAccount principal, String token, Collection<? extends GrantedAuthority> authorities)
	{
		super(principal, "N/A", authorities);
		myToken = token;
	}

	public String getToken()
	{
		return myToken;
	}

	@Override
	public UserAccount getPrincipal()
	{
		return (UserAccount) super.getPrincipal();
	}
}
