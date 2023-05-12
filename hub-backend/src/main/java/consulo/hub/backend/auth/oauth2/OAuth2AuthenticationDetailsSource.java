package consulo.hub.backend.auth.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

/**
 * @author VISTALL
 * @since 09/05/2023
 */
public class OAuth2AuthenticationDetailsSource implements AuthenticationDetailsSource<HttpServletRequest, WebAuthenticationDetails>
{
	@Override
	public WebAuthenticationDetails buildDetails(HttpServletRequest context)
	{
		return new OAuth2AuthenticationDetails(context);
	}
}
