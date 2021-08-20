package consulo.webService.auth;


import com.fasterxml.jackson.databind.ObjectMapper;
import consulo.webService.auth.oauth2.OAuth2ServerConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

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
			request.setClientId(OAuth2ServerConfiguration.DEFAULT_CLIENT_ID);

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

	@RequestMapping(value = "/api/auth/validate", method = RequestMethod.GET)
	public ResponseEntity<?> validate(@RequestParam("email") String email, @RequestHeader("Authorization") String authorization)
	{
		Collection<OAuth2AccessToken> tokensByClientId = myTokenStore.findTokensByClientId(OAuth2ServerConfiguration.DEFAULT_CLIENT_ID);
		for(OAuth2AccessToken oAuth2AccessToken : tokensByClientId)
		{
			System.out.println(oAuth2AccessToken.getValue());
		}

		Authentication target = myTokenStore.readAuthentication(authorization);
		if(target == null)
		{
			target = new AnonymousAuthenticationToken("anonym", "anonym", List.of(new SimpleGrantedAuthority(Roles.ROLE_ANONYM)));
		}

		Authentication authenticate = myAuthenticationManager.authenticate(target);

		if(authenticate == null)
		{
			throw new IllegalArgumentException();
		}

		//		OAuth2AuthenticationAccessToken token = myOAuth2AccessTokenRepository.findByTokenId(authorization);
		//		if(token == null)
		//		{
		//			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		//		}
		//
		//		email = StringUtil.unquoteString(email);
		//
		//		if(!token.getUserName().equals(email))
		//		{
		//			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		//		}
		return ResponseEntity.ok().build();
	}
}
