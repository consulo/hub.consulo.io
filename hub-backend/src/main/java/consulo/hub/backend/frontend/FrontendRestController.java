package consulo.hub.backend.frontend;

import consulo.hub.backend.auth.repository.UserAccountRepository;
import consulo.hub.shared.ServiceAccounts;
import consulo.hub.shared.auth.domain.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author VISTALL
 * @since 22/08/2021
 */
@RestController
public class FrontendRestController
{
	@Autowired
	private UserAccountRepository myUserAccountRepository;

//	@Autowired
//	private TokenStore myTokenStore;

//	@Autowired
//	private AuthenticationManager myAuthenticationManager;
//
//	@Autowired
//	private OAuth2RequestFactory myOAuth2RequestFactory;
//
//	@Autowired
//	private AuthorizationServerTokenServices myAuthorizationServerTokenServices;

	@Autowired
	private PasswordEncoder myPasswordEncoder;

	private boolean myInstalled;

	@RequestMapping("/api/private/test")
	public ResponseEntity<?> test()
	{
		return ResponseEntity.ok(Map.of("result", "OK"));
	}

	@RequestMapping("/api/private/config/jenkins")
	public Map<String, String> jenkins()
	{
		UserAccount account = myUserAccountRepository.findByUsername(ServiceAccounts.JENKINS_DEPLOY);

		if(account == null)
		{
			return Map.of();
		}

//		Collection<OAuth2AccessToken> tokens = myTokenStore.findTokensByClientIdAndUserName(ServiceClientId.JENKINS_CLIENT_ID, account.getUsername());
//
//		if(tokens.isEmpty())
//		{
//			return Map.of();
//		}
//
//		OAuth2AccessToken token = tokens.iterator().next();
//
//		return Map.of(account.getUsername(), token.getValue());
		return null;
	}

	@RequestMapping("/api/private/install")
	public ResponseEntity<?> install()
	{
//		if(myInstalled)
//		{
//			return ResponseEntity.badRequest().build();
//		}
//
//		UserAccount account = myUserAccountRepository.findByUsername(ServiceAccounts.HUB);
//
//		if(account != null)
//		{
//			Collection<OAuth2AccessToken> tokens = myTokenStore.findTokensByClientIdAndUserName(ServiceClientId.HUB_CLIENT_ID, account.getUsername());
//			if(!tokens.isEmpty())
//			{
//				myInstalled = true;
//				return ResponseEntity.badRequest().build();
//			}
//
//			myUserAccountRepository.delete(account);
//		}
//
//		// we not interest in password
//		String password = RandomStringUtils.randomAlphanumeric(48);
//
//		account = new UserAccount();
//		account.setUsername(ServiceAccounts.HUB);
//		account.setRights(UserAccount.ROLE_HUB);
//		account.setStatus(UserAccountStatus.STATUS_APPROVED);
//
//		account.setPassword(myPasswordEncoder.encode(password));
//
//		account = myUserAccountRepository.save(account);
//
//		Authentication authenticate = myAuthenticationManager.authenticate(new UsernamePasswordAuthenticationToken(account.getUsername(), password));
//
//		AuthorizationRequest request = new AuthorizationRequest();
//		request.setClientId(ServiceClientId.HUB_CLIENT_ID);
//
//		OAuth2Request auth2Request = myOAuth2RequestFactory.createOAuth2Request(request);
//
//		OAuth2Authentication authentication = new OAuth2Authentication(auth2Request, authenticate);
//
//		OAuth2AccessToken accessToken = myAuthorizationServerTokenServices.createAccessToken(authentication);
//
//		myTokenStore.storeAccessToken(accessToken, authentication);
//
//		Map<String, String> map = new TreeMap<>();
//		map.put("token", accessToken.getValue());

//		return ResponseEntity.ok(map);
		return null;
	}
}
