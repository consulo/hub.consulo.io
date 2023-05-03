package consulo.hub.backend.frontend;

import com.fasterxml.jackson.databind.ObjectMapper;
import consulo.hub.backend.auth.LocalAuthenticationProvider;
import consulo.hub.backend.auth.UserAccountService;
import consulo.hub.backend.auth.repository.UserAccountRepository;
import consulo.hub.shared.auth.domain.UserAccount;
import consulo.hub.shared.auth.oauth2.domain.OAuthTokenInfo;
import consulo.hub.shared.auth.rest.UserAuthResult;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author VISTALL
 * @since 22/08/2021
 */
@RestController
public class FrontendUserRestController
{
	private static final Logger LOG = LoggerFactory.getLogger(FrontendUserRestController.class);

	@Autowired
	private LocalAuthenticationProvider myLocalAuthenticationProvider;

	@Autowired
	private UserAccountService myUserAccountService;

//	@Autowired
//	private TokenStore myTokenStore;

	@Autowired
	private PasswordEncoder myPasswordEncoder;

//	@Autowired
//	private AuthorizationServerTokenServices myAuthorizationServerTokenServices;
//
//	@Autowired
//	private OAuth2RequestFactory myOAuth2RequestFactory;

	@Autowired
	private ObjectMapper myObjectMapper;

	@Autowired
	private UserAccountRepository myUserAccountRepository;

//	@Autowired
//	private OAuthKeyRequestService myOAuthKeyRequestService;

	@RequestMapping("/api/private/user/register")
	public UserAccount registerUser(@RequestParam("email") String email, @RequestParam("password") String password, @AuthenticationPrincipal UserAccount hub)
	{
		UserAccount userAccount = myUserAccountService.registerUser(email, password);
		return Objects.requireNonNull(userAccount, "null is not allowed");
	}

	@RequestMapping("/api/private/user/list")
	public List<UserAccount> listUsers(@AuthenticationPrincipal UserAccount hub)
	{
		return myUserAccountRepository.findAll();
	}

	@RequestMapping("/api/private/user/auth")
	public UserAuthResult userAuth(@RequestParam("email") String email, @RequestParam("password") String password, @AuthenticationPrincipal UserAccount hub)
	{
		UserDetails userDetails = myLocalAuthenticationProvider.retrieveUser(email, new UsernamePasswordAuthenticationToken(email, password));

		return new UserAuthResult((UserAccount) userDetails, RandomStringUtils.randomAlphanumeric(32));
	}

	@RequestMapping("/api/private/user/oauth/request")
	public Map<String, String> requestKey(@RequestParam("userId") long userId, @RequestParam("token") String token, @RequestParam("hostName") String hostName)
	{
		DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

		//myOAuthKeyRequestService.addRequest(userId, token, hostName);
		return Map.of("token", token);
	}

	@RequestMapping("/api/private/user/changePassword")
	public UserAccount userChangePassword(@RequestParam("userId") long userId, @RequestParam("oldPassword") String oldPassword, @RequestParam("newPassword") String newPassword)
	{
		return myUserAccountService.changePassword(userId, oldPassword, newPassword);
	}

	@RequestMapping("/api/private/user/oauth/list")
	public List<OAuthTokenInfo> userOAuthKeys(@RequestParam("userId") long userId, @AuthenticationPrincipal UserAccount hub)
	{
		UserAccount user = myUserAccountService.findUser(userId);
		if(user == null)
		{
			throw new IllegalArgumentException("Can't find user by id: " + userId);
		}

//		Collection<OAuth2AccessToken> tokens = myTokenStore.findTokensByClientIdAndUserName(ServiceClientId.CONSULO_CLIENT_ID, user.getUsername());
//
//		List<OAuthTokenInfo> list = new ArrayList<>();
//		for(OAuth2AccessToken token : tokens)
//		{
//			list.add(new OAuthTokenInfo(token.getValue(), Map.copyOf(token.getAdditionalInformation())));
//		}
//
//		return list;
		return List.of();
	}

	@RequestMapping("/api/private/user/oauth/add")
	public OAuthTokenInfo userOAuthKeyAdd(@RequestParam("userId") long userId, @RequestParam("name") String name, @AuthenticationPrincipal UserAccount hub)
	{
		UserAccount user = myUserAccountService.findUser(userId);
		if(user == null)
		{
			throw new IllegalArgumentException("Can't find user by id: " + userId);
		}

		return null;
//		try
//		{
//			UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(user, name, user.getAuthorities());
//
//			AuthorizationRequest request = new AuthorizationRequest();
//			request.setClientId(ServiceClientId.CONSULO_CLIENT_ID);
//
//			OAuth2Request auth2Request = myOAuth2RequestFactory.createOAuth2Request(request);
//
//			OAuth2Authentication authentication = new OAuth2Authentication(auth2Request, token);
//
//			OAuth2AccessToken accessToken = myAuthorizationServerTokenServices.createAccessToken(authentication);
//
//			myTokenStore.storeAccessToken(accessToken, authentication);
//
//			String value = accessToken.getValue();
//
//			return new OAuthTokenInfo(value, Map.copyOf(accessToken.getAdditionalInformation()));
//		}
//		catch(Exception e)
//		{
//			LOG.warn("UserId: " + userId, e);
//			throw e;
//		}
	}

	@RequestMapping("/api/private/user/oauth/remove")
	public OAuthTokenInfo userOAuthKeyRemove(@RequestParam("userId") long userId, @RequestParam("token") String token, @AuthenticationPrincipal UserAccount hub)
	{
		UserAccount user = myUserAccountService.findUser(userId);
		if(user == null)
		{
			throw new IllegalArgumentException("Can't find user by id: " + userId);
		}

		return null;
//		try
//		{
//			OAuth2Authentication authentication = Objects.requireNonNull(myTokenStore.readAuthentication(token));
//
//			if(!Objects.equals(user, authentication.getPrincipal()))
//			{
//				throw new IllegalArgumentException("wrong user: " + user.getId() + "/" + authentication.getPrincipal());
//			}
//
//			OAuth2AccessToken accessToken = Objects.requireNonNull(myTokenStore.readAccessToken(token));
//
//			myTokenStore.removeAccessToken(accessToken);
//
//			return new OAuthTokenInfo(token, Map.copyOf(accessToken.getAdditionalInformation()));
//		}
//		catch(Exception e)
//		{
//			LOG.warn("UserId: " + userId, e);
//			throw e;
//		}
	}
}
