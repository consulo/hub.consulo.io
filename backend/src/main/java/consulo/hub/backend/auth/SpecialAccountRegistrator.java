package consulo.hub.backend.auth;

import consulo.hub.backend.auth.repository.UserAccountRepository;
import consulo.hub.shared.ServiceAccounts;
import consulo.hub.shared.auth.domain.UserAccount;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * @author VISTALL
 * @since 03/09/2021
 */
@Service
public class SpecialAccountRegistrator
{
	@Autowired
	private UserAccountService myUserAccountService;

	@Autowired
	private UserAccountRepository myUserRepository;

	@Autowired
	private PasswordEncoder myPasswordEncoder;

	@PostConstruct
	public void check() throws IOException
	{
		registerUserIfNeed(ServiceAccounts.JENKINS_DEPLOY, UserAccount.ROLE_SUPERDEPLOYER);
		registerUserIfNeed(ServiceAccounts.HUB, UserAccount.ROLE_HUB);
	}

	private void registerUserIfNeed(String email, int rights) throws IOException
	{
		UserAccount user = myUserAccountService.findUser(email);
		if(user == null)
		{
			String password = savePassword(email);

			myUserAccountService.registerUser(email, password, rights);
		}
		else if(user.getPassword() == null)
		{
			String newPassword = savePassword(email);

			user.setPassword(myPasswordEncoder.encode(newPassword));

			myUserRepository.saveAndFlush(user);
		}
	}

	private String savePassword(String email) throws IOException
	{
		String password = RandomStringUtils.randomAlphanumeric(32);

		Path path = Path.of(email.replace("@", "__").replace(".", "_") + ".txt");

		Files.deleteIfExists(path);

		Files.write(path, List.of(password));

		return password;
	}
}
