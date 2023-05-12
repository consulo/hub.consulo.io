package consulo.procoeton.core.auth;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * @author VISTALL
 * @since 11/05/2023
 */
public class StubUserDetailsService implements UserDetailsService
{
	public static final StubUserDetailsService INSTANCE = new StubUserDetailsService();

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
	{
		throw new UsernameNotFoundException(username);
	}
}
