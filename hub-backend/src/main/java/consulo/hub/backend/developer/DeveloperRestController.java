package consulo.hub.backend.developer;

import consulo.hub.backend.auth.repository.UserAccountRepository;
import consulo.hub.shared.auth.domain.UserAccount;
import consulo.util.lang.BitUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @since 29/08/2021
 */
@RestController
public class DeveloperRestController
{
	@Autowired
	private UserAccountRepository myUserAccountRepository;

	@RequestMapping(value = "/api/developer/list", method = RequestMethod.GET)
	public List<UserAccount> developerList(@AuthenticationPrincipal UserAccount account)
	{
		List<UserAccount> all = myUserAccountRepository.findAll();

		List<UserAccount> developers = new ArrayList<>();
		for(UserAccount userAccount : all)
		{
			if(BitUtil.isSet(userAccount.getRights(), UserAccount.ROLE_DEVELOPER))
			{
				developers.add(account);
			}
		}
		return developers;
	}
}
