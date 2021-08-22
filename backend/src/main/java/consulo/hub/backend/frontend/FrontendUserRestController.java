package consulo.hub.backend.frontend;

import consulo.hub.shared.auth.domain.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author VISTALL
 * @since 22/08/2021
 */
@RestController
public class FrontendUserRestController
{
	@Autowired
	private AuthenticationManager myAuthenticationManager;

	@RequestMapping("/api/private/user/auth")
	public UserAccount userAuth(@RequestParam("email") String email, @RequestParam("password") String password, @AuthenticationPrincipal UserAccount userAccount)
	{
		Authentication authenticate = myAuthenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));

		return (UserAccount) authenticate.getPrincipal();
	}
}
