package consulo.hub.backend.auth;

import consulo.hub.backend.auth.repository.UserAccountRepository;
import consulo.hub.shared.auth.domain.UserAccount;
import consulo.hub.shared.auth.domain.UserAccountStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import jakarta.annotation.Nullable;

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
		return myUserRepository.findById(userId).orElse(null);
	}

	@Nullable
	public UserAccount findUser(String email)
	{
		return myUserRepository.findByUsername(email);
	}

	@Nullable
	public UserAccount registerUser(String username, String password)
	{
		return registerUser(username, password, 0);
	}

	@Nullable
	public UserAccount registerUser(String username, String password, int right)
	{
		UserAccount user = new UserAccount();
		user.setUsername(username);
		user.setPassword(myPasswordEncoder.encode(password));
		user.setRights(right);
		user.setStatus(UserAccountStatus.STATUS_DISABLED);

		if(!create(user))
		{
			return null;
		}

		return save(user);
	}

	public UserAccount changePassword(long userId, String oldPassword, String newPassword)
	{
		UserAccount account = findUser(userId);
		if(account == null)
		{
			return null;
		}

		if(!myPasswordEncoder.matches(oldPassword, account.getPassword()))
		{
			return null;
		}

		account.setPassword(myPasswordEncoder.encode(newPassword));
		return myUserRepository.save(account);
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
