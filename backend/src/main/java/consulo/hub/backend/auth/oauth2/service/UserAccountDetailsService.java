package consulo.hub.backend.auth.oauth2.service;

import consulo.hub.backend.auth.service.UserAccountService;
import consulo.hub.shared.auth.domain.UserAccount;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * @author VISTALL
 * @since 26-Sep-16
 */
public class UserAccountDetailsService implements UserDetailsService
{
	private final UserAccountService userRepository;

	public UserAccountDetailsService(UserAccountService userRepository)
	{
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
	{
		UserAccount user = userRepository.getByUsername(username);
		if(user == null)
		{
			throw new UsernameNotFoundException(String.format("User %s does not exist!", username));
		}
		return user;
	}
}
