package consulo.webService.auth;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.intellij.openapi.util.text.StringUtil;
import consulo.webService.auth.oauth2.domain.OAuth2AuthenticationAccessToken;
import consulo.webService.auth.oauth2.mongo.OAuth2AccessTokenRepository;

/**
 * @author VISTALL
 * @since 10-Mar-17
 */
@RestController
public class AuthRestController
{
	@Autowired
	private OAuth2AccessTokenRepository myOAuth2AccessTokenRepository;

	@RequestMapping(value = "/api/auth/validate", method = RequestMethod.GET)
	public ResponseEntity<?> validate(@RequestParam("email") String email, @RequestHeader("Authorization") String authorization)
	{
		OAuth2AuthenticationAccessToken token = myOAuth2AccessTokenRepository.findByTokenId(authorization);
		if(token == null)
		{
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		email = StringUtil.unquoteString(email);

		if(!token.getUserName().equals(email))
		{
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		return ResponseEntity.ok().build();
	}
}
