package consulo.hub.backend.auth.oauth2.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import java.io.Serializable;
import java.util.Date;

/**
 * @version 1.0
 * @author: Iain Porter
 * @since 22/05/2013
 */
@Document(collection = "oauth2AccessToken")
@Deprecated
public class OAuth2AuthenticationAccessToken implements Serializable
{
	@Id
	private String tokenId;
	private OAuth2AccessToken oAuth2AccessToken;
	private String authenticationId;
	private String userName;
	private String clientId;
	private String name;
	private Date timeCreated = new Date();

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

	public Date getTimeCreated()
	{
		return timeCreated;
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

	@Override
	public boolean equals(Object o)
	{
		if(this == o)
		{
			return true;
		}
		if(!(o instanceof OAuth2AuthenticationAccessToken))
		{
			return false;
		}

		OAuth2AuthenticationAccessToken token = (OAuth2AuthenticationAccessToken) o;

		if(tokenId != null ? !tokenId.equals(token.tokenId) : token.tokenId != null)
		{
			return false;
		}

		return true;
	}

	@Override
	public int hashCode()
	{
		return tokenId != null ? tokenId.hashCode() : 0;
	}
}