package consulo.hub.backend.frontend;

import consulo.hub.backend.auth.repository.UserAccountRepository;
import consulo.hub.shared.ServiceAccounts;
import consulo.hub.shared.auth.domain.UserAccount;
import consulo.hub.shared.auth.domain.UserAccountStatus;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author VISTALL
 * @since 22/08/2021
 */
@RestController
public class FrontendRestController
{
	public static final String HUB_CLIENT_ID = "hub";

	@Autowired
	private UserAccountRepository myUserAccountRepository;

	@Autowired
	private TokenStore myTokenStore;

	@Autowired
	private AuthenticationManager myAuthenticationManager;

	@Autowired
	private OAuth2RequestFactory myOAuth2RequestFactory;

	@Autowired
	private AuthorizationServerTokenServices myAuthorizationServerTokenServices;

	@Autowired
	private PasswordEncoder myPasswordEncoder;

	private boolean myInstalled;

	@RequestMapping("/private/api/install")
	public ResponseEntity<?> install()
	{
		if(myInstalled)
		{
			return ResponseEntity.badRequest().build();
		}

		UserAccount account = myUserAccountRepository.findByUsername(ServiceAccounts.HUB);

		if(account != null)
		{
			Collection<OAuth2AccessToken> tokens = myTokenStore.findTokensByClientIdAndUserName(HUB_CLIENT_ID, account.getUsername());
			if(!tokens.isEmpty())
			{
				myInstalled = true;
				return ResponseEntity.badRequest().build();
			}

			myUserAccountRepository.delete(account);
		}

		// we not interest in password
		String password = RandomStringUtils.randomAlphanumeric(48);

		account = new UserAccount();
		account.setUsername(ServiceAccounts.HUB);
		account.setRights(UserAccount.ROLE_HUB);
		account.setStatus(UserAccountStatus.STATUS_APPROVED);

		account.setPassword(myPasswordEncoder.encode(password));

		account = myUserAccountRepository.save(account);

		Authentication authenticate = myAuthenticationManager.authenticate(new UsernamePasswordAuthenticationToken(account.getUsername(), password));

		AuthorizationRequest request = new AuthorizationRequest();
		request.setClientId(HUB_CLIENT_ID);

		OAuth2Request auth2Request = myOAuth2RequestFactory.createOAuth2Request(request);

		OAuth2Authentication authentication = new OAuth2Authentication(auth2Request, authenticate);

		OAuth2AccessToken accessToken = myAuthorizationServerTokenServices.createAccessToken(authentication);

		myTokenStore.storeAccessToken(accessToken, authentication);

		Map<String, String> map = new TreeMap<>();
		map.put("token", accessToken.getValue());

		return ResponseEntity.ok(map);
	}

}
