package consulo.hub.backend.auth.oauth2.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author VISTALL
 * @since 20/08/2021
 */
@Entity
@Table(name = "oauth_access_token")
public class JpaOAuthAccessToken
{
	//token_id, token, authentication_id, user_name, client_id, authentication, refresh_token

	@Id
	private String tokenId;
	private byte[] token;
	private String authenticationId;
	private String userName;
	private String clientId;
	private byte[] authentication;
	private String refreshToken;

	public String getTokenId()
	{
		return tokenId;
	}

	public void setTokenId(String tokenId)
	{
		this.tokenId = tokenId;
	}

	public byte[] getToken()
	{
		return token;
	}

	public void setToken(byte[] token)
	{
		this.token = token;
	}

	public String getAuthenticationId()
	{
		return authenticationId;
	}

	public void setAuthenticationId(String authenticationId)
	{
		this.authenticationId = authenticationId;
	}

	public String getUserName()
	{
		return userName;
	}

	public void setUserName(String userName)
	{
		this.userName = userName;
	}

	public String getClientId()
	{
		return clientId;
	}

	public void setClientId(String clientId)
	{
		this.clientId = clientId;
	}

	public byte[] getAuthentication()
	{
		return authentication;
	}

	public void setAuthentication(byte[] authentication)
	{
		this.authentication = authentication;
	}

	public String getRefreshToken()
	{
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken)
	{
		this.refreshToken = refreshToken;
	}
}
