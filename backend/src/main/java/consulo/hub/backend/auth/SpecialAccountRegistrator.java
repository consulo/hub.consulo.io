package consulo.hub.backend.auth;

import consulo.hub.backend.auth.service.UserAccountService;
import consulo.hub.shared.ServiceAccounts;
import consulo.hub.shared.ServiceClientId;
import consulo.hub.shared.auth.domain.UserAccount;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Service;

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
	private AuthenticationManager myAuthenticationManager;

	@Autowired
	private TokenStore myTokenStore;

	@Autowired
	private OAuth2RequestFactory myOAuth2RequestFactory;

	@Autowired
	private AuthorizationServerTokenServices myAuthorizationServerTokenServices;

	@PostConstruct
	public void check()
	{
		UserAccount user = myUserAccountService.findUser(ServiceAccounts.JENKINS_DEPLOY);
		if(user == null)
		{
			String password = RandomStringUtils.randomAlphanumeric(32);

			user = myUserAccountService.registerUser(ServiceAccounts.JENKINS_DEPLOY, password, UserAccount.ROLE_SUPERDEPLOYER);

			assert user != null;
			
			Authentication authenticate = myAuthenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), password));

			AuthorizationRequest request = new AuthorizationRequest();
			request.setClientId(ServiceClientId.JENKINS_CLIENT_ID);

			OAuth2Request auth2Request = myOAuth2RequestFactory.createOAuth2Request(request);

			OAuth2Authentication authentication = new OAuth2Authentication(auth2Request, authenticate);

			OAuth2AccessToken accessToken = myAuthorizationServerTokenServices.createAccessToken(authentication);

			myTokenStore.storeAccessToken(accessToken, authentication);
		}
	}
}
