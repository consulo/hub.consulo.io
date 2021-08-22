package consulo.hub.backend.auth.service;

import consulo.hub.backend.auth.repository.UserAccountRepository;
import consulo.hub.shared.auth.domain.UserAccount;
import consulo.hub.shared.auth.domain.UserAccountStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class LocalAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider
{
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private UserAccountRepository myUserAccountRepository;

	@Autowired
	private PasswordEncoder encoder;

	@Override
	protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException
	{
	}

	@Override
	public UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException
	{
		String password = (String) authentication.getCredentials();
		if(!StringUtils.hasText(password))
		{
			logger.warn("Username {}: no password provided", username);
			throw new BadCredentialsException("Please enter password");
		}

		UserAccount user = myUserAccountRepository.findByUsername(username);
		if(user == null)
		{
			logger.warn("Username {} password {}: user not found", username, password);
			throw new UsernameNotFoundException("Invalid Login");
		}

		if(!encoder.matches(password, user.getPassword()))
		{
			logger.warn("Username {} password {}: invalid password", username, password);
			throw new BadCredentialsException("Invalid Login");
		}

		if(user.getStatus() != UserAccountStatus.STATUS_APPROVED)
		{
			logger.warn("Username {}: not approved", username);
			throw new BadCredentialsException("User has not been approved");
		}

		return user;
	}

}
