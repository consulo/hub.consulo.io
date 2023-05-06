package consulo.hub.backend.auth;

import consulo.hub.backend.auth.repository.UserAccountRepository;
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
	private final UserAccountRepository myUserRepository;

	public UserAccountDetailsService(UserAccountRepository userRepository)
	{
		myUserRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
	{
		UserAccount user = myUserRepository.findByUsername(username);
		if(user == null)
		{
			throw new UsernameNotFoundException(String.format("User %s does not exist!", username));
		}
		return user;
	}
}
