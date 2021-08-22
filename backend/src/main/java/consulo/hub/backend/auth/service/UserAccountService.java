package consulo.hub.backend.auth.service;

import consulo.hub.backend.auth.repository.UserAccountRepository;
import consulo.hub.shared.auth.domain.UserAccount;
import consulo.hub.shared.auth.domain.UserAccountStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class UserAccountService
{
	@Autowired
	private UserAccountRepository userRepository;

	@Autowired
	private PasswordEncoder myPasswordEncoder;

	public boolean registerUser(String username, String password)
	{
		UserAccount user = new UserAccount();
		user.setUsername(username);
		user.setPassword(myPasswordEncoder.encode(password));
		user.setRights(0);
		user.setStatus(UserAccountStatus.STATUS_DISABLED);

		if(!create(user))
		{
			return false;
		}

		save(user);
		return true;
	}

	public boolean changePassword(String username, String oldPassword, String newPassword)
	{
		UserAccount account = getByUsername(username);
		if(account == null)
		{
			return false;
		}

		if(!myPasswordEncoder.matches(oldPassword, account.getPassword()))
		{
			return false;
		}

		account.setPassword(myPasswordEncoder.encode(newPassword));
		userRepository.save(account);
		return true;
	}

	public boolean create(UserAccount user)
	{
		Assert.isNull(user.getId(), "userId must be null");

		// duplicate username
		if(userRepository.findByUsername(user.getUsername()) != null)
		{
			return false;
		}

		user.setStatus(UserAccountStatus.STATUS_APPROVED);
		userRepository.save(user);
		return true;
	}

	public void save(UserAccount user)
	{
		Assert.notNull(user.getId(), "userId must be not null");
		userRepository.save(user);
	}

	public void delete(UserAccount user)
	{
		Assert.notNull(user.getId(), "userId must be not null");
		userRepository.delete(user);
	}

	public UserAccount getByUsername(String username)
	{
		return userRepository.findByUsername(username);
	}
}
