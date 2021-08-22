package consulo.hub.frontend.backend.service;

import consulo.hub.shared.auth.domain.UserAccount;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author VISTALL
 * @since 21/08/2021
 */
@Service
public class UserAccountService
{
	public boolean registerUser(String userName, String password)
	{
		// TODO
		throw new UnsupportedOperationException();
	}

	public boolean changePassword(String userName, String oldPassword, String newPassword)
	{
		// TODO
		throw new UnsupportedOperationException();
	}

	public List<UserAccount> listAll()
	{
		// TODO
		throw new UnsupportedOperationException();
	}
}
