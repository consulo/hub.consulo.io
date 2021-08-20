package consulo.webService.auth.service;

import consulo.webService.auth.domain.UserAccount;
import consulo.webService.auth.domain.UserAccountStatus;
import consulo.webService.auth.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class UserAccountService
{
	@Autowired
	private UserAccountRepository userRepository;

	@Autowired
	private PasswordEncoder myPasswordEncoder;

	@Autowired
	private MongoOperations myMongoOperations;

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
