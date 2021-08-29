package consulo.hub.backend.auth.service;

import consulo.hub.backend.auth.repository.UserAccountRepository;
import consulo.hub.shared.auth.domain.UserAccount;
import consulo.hub.shared.auth.domain.UserAccountStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.Nullable;

@Component
public class UserAccountService
{
	@Autowired
	private UserAccountRepository myUserRepository;

	@Autowired
	private PasswordEncoder myPasswordEncoder;

	@Nullable
	public UserAccount findUser(long userId)
	{
		return myUserRepository.findOne(userId);
	}

	@Nullable
	public UserAccount registerUser(String username, String password)
	{
		UserAccount user = new UserAccount();
		user.setUsername(username);
		user.setPassword(myPasswordEncoder.encode(password));
		user.setRights(0);
		user.setStatus(UserAccountStatus.STATUS_DISABLED);

		if(!create(user))
		{
			return null;
		}

		return save(user);
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
		myUserRepository.save(account);
		return true;
	}

	public boolean create(UserAccount user)
	{
		Assert.isNull(user.getId(), "userId must be null");

		// duplicate username
		if(myUserRepository.findByUsername(user.getUsername()) != null)
		{
			return false;
		}

		user.setStatus(UserAccountStatus.STATUS_APPROVED);
		myUserRepository.save(user);
		return true;
	}

	public UserAccount save(UserAccount user)
	{
		Assert.notNull(user.getId(), "userId must be not null");
		return myUserRepository.save(user);
	}

	public void delete(UserAccount user)
	{
		Assert.notNull(user.getId(), "userId must be not null");
		myUserRepository.delete(user);
	}

	public UserAccount getByUsername(String username)
	{
		return myUserRepository.findByUsername(username);
	}
}
