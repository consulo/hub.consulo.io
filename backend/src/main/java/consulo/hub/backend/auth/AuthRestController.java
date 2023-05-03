package consulo.hub.backend.auth;


import com.google.common.base.Objects;
import consulo.hub.shared.auth.domain.UserAccount;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
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
	//@Autowired
	//private OAuthKeyRequestService myOAuthKeyRequestService;

	@RequestMapping(value = "/api/oauth/request", method = RequestMethod.GET)
	public ResponseEntity<?> requestKey(@RequestParam("token") String token)
	{
//		OAuthRequestResult result = myOAuthKeyRequestService.doRequest(token);
//		if(result == null)
//		{
//			return ResponseEntity.notFound().build();
//		}
//		return ResponseEntity.ok(result);
		return null;
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
