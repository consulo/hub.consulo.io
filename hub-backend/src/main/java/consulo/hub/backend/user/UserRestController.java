package consulo.hub.backend.user;

import consulo.hub.shared.auth.domain.UserAccount;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * @author VISTALL
 * @since 04/05/2023
 */
@RestController
public class UserRestController
{
	@RequestMapping(value = "/api/user/info", method = RequestMethod.GET)
	public UserAccount infoAll(@AuthenticationPrincipal UserAccount account) throws IOException
	{
		return account;
	}
}
