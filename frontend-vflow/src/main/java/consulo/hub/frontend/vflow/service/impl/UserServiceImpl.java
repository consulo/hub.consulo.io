package consulo.hub.frontend.vflow.service.impl;

import consulo.hub.frontend.vflow.service.UserService;
import consulo.hub.shared.auth.SecurityUtil;
import consulo.hub.shared.auth.domain.UserAccount;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author VISTALL
 * @since 30/04/2023
 */
@Service
public class UserServiceImpl implements UserService
{
	@Override
	public Optional<UserAccount> getCurrentUser()
	{
		UserAccount userAccout = SecurityUtil.getUserAccout();
		return Optional.ofNullable(userAccout);
	}

	@Override
	public void logout()
	{
		// TODO
	}
}