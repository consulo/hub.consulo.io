package consulo.webService.auth.oauth2.domain;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

/**
 * @version 1.0
 * @author: Iain Porter
 * @since 22/05/2013
 */
@Document(collection = "oauth2AccessToken")
public class OAuth2AuthenticationAccessToken extends BaseEntity
{
	private String tokenId;
	private OAuth2AccessToken oAuth2AccessToken;
	private String authenticationId;
	private String userName;
	private String clientId;
	private String name;

	public OAuth2AuthenticationAccessToken()
	{
	}

	public OAuth2AuthenticationAccessToken(final OAuth2AccessToken oAuth2AccessToken, final OAuth2Authentication authentication, final String authenticationId)
	{
		this.tokenId = oAuth2AccessToken.getValue();
		this.oAuth2AccessToken = oAuth2AccessToken;
		this.authenticationId = authenticationId;
		this.userName = authentication.getName();
		this.clientId = authentication.getOAuth2Request().getClientId();
		this.name = (String) authentication.getOAuth2Request().getExtensions().get("name");
	}

	public String getName()
	{
		return name;
	}

	public String getTokenId()
	{
		return tokenId;
	}

	public OAuth2AccessToken getoAuth2AccessToken()
	{
		return oAuth2AccessToken;
	}

	public String getAuthenticationId()
	{
		return authenticationId;
	}

	public String getUserName()
	{
		return userName;
	}

	public String getClientId()
	{
		return clientId;
	}
}