package consulo.hub.backend.auth.oauth2.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import consulo.hub.shared.auth.domain.UserAccount;
import consulo.hub.backend.auth.service.UserAccountService;

/**
 * @author VISTALL
 * @since 26-Sep-16
 */
@Service
public class UserAccountDetailsService implements UserDetailsService
{
	private final UserAccountService userRepository;

	@Autowired
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
