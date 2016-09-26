package consulo.webService.auth.mongo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import consulo.webService.auth.mongo.domain.Role;
import consulo.webService.auth.mongo.domain.UserAccount;
import consulo.webService.auth.mongo.domain.UserAccountStatus;
import consulo.webService.auth.mongo.repository.RoleRepository;
import consulo.webService.auth.mongo.repository.UserAccountRepository;

@Service
public class UserService
{

	@Autowired
	private UserAccountRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private PasswordEncoder myPasswordEncoder;

	@Autowired
	private MongoOperations myMongoOperations;

	public boolean registerUser(String username, String password)
	{
		UserAccount user = new UserAccount();
		user.setUsername(username);
		user.setPassword(myPasswordEncoder.encode(password));

		//user.addRole(getRole("ROLE_ADMIN"));
		user.addRole(getRole("ROLE_USER"));

		if(!create(user))
		{
			return false;
		}

		user.setEnabled(true);
		user.setStatus(UserAccountStatus.STATUS_APPROVED.name());

		save(user);
		return true;
	}

	public Role getRole(String role)
	{
		return roleRepository.findOne(role);
	}

	public boolean create(UserAccount user)
	{
		Assert.isNull(user.getId());

		// duplicate username
		if(userRepository.findByUsername(user.getUsername()) != null)
		{
			return false;
		}
		user.setEnabled(false);
		user.setStatus(UserAccountStatus.STATUS_DISABLED.name());
		userRepository.save(user);
		return true;
	}

	public void save(UserAccount user)
	{
		Assert.notNull(user.getId());
		userRepository.save(user);
	}

	public void delete(UserAccount user)
	{
		Assert.notNull(user.getId());
		userRepository.delete(user);
	}

	public UserAccount getByUsername(String username)
	{
		return userRepository.findByUsername(username);
	}

}
