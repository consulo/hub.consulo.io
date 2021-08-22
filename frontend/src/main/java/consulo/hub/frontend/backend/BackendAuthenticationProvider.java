package consulo.hub.frontend.backend;

import consulo.hub.shared.auth.domain.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author VISTALL
 * @since 22/08/2021
 */
@Component
public class BackendAuthenticationProvider implements AuthenticationProvider
{
	@Autowired
	private BackendRequestor myBackendRequestor;

	@Override
	public Authentication authenticate(Authentication temp) throws AuthenticationException
	{
		UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) temp;

		try
		{
			Map<String, String> map = new HashMap<>();
			map.put("email", authentication.getName());
			map.put("password", (String) authentication.getCredentials());

			UserAccount account = myBackendRequestor.runRequest("/user/auth", map, UserAccount.class);
			return new UsernamePasswordAuthenticationToken(account, "N/A", account.getAuthorities());
		}
		catch(Exception e)
		{
			throw new BadCredentialsException("bad credentials", e);
		}
	}

	@Override
	public boolean supports(Class<?> authentication)
	{
		return authentication == UsernamePasswordAuthenticationToken.class;
	}
}
