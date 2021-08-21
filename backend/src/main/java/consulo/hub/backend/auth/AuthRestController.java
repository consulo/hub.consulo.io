package consulo.hub.backend.auth;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Objects;
import consulo.hub.backend.ServiceConstants;
import consulo.hub.shared.auth.domain.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author VISTALL
 * @since 10-Mar-17
 */
@RestController
public class AuthRestController
{
	@Autowired
	private OAuth2RequestFactory myOAuth2RequestFactory;

	@Autowired
	private AuthenticationManager myAuthenticationManager;

	@Autowired
	private AuthorizationServerTokenServices myAuthorizationServerTokenServices;

	@Autowired
	private TokenStore myTokenStore;

	@RequestMapping(value = "/api/oauth/auth", method = RequestMethod.GET)
	public ResponseEntity<?> oauthAuth(@RequestParam("email") String email, @RequestParam("password") String password)
	{
		try
		{
			Authentication authenticate = myAuthenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));

			AuthorizationRequest request = new AuthorizationRequest();
			request.setClientId(ServiceConstants.DEFAULT_CLIENT_ID);

			OAuth2Request auth2Request = myOAuth2RequestFactory.createOAuth2Request(request);

			OAuth2Authentication authentication = new OAuth2Authentication(auth2Request, authenticate);

			OAuth2AccessToken accessToken = myAuthorizationServerTokenServices.createAccessToken(authentication);

			ObjectMapper objectMapper = new ObjectMapper();

			String string = objectMapper.writeValueAsString(accessToken);


			myTokenStore.storeAccessToken(accessToken, authentication);

			System.out.println("test");
		}
		catch(Exception e)
		{
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}

		return ResponseEntity.ok().build();
	}

	@RequestMapping(value = "/api/oauth/validate", method = RequestMethod.GET)
	public ResponseEntity<?> validate(@RequestParam("email") String email, @AuthenticationPrincipal UserAccount account)
	{
		if(!Objects.equal(account.getUsername(), email))
		{
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		return ResponseEntity.ok().build();
	}
}
