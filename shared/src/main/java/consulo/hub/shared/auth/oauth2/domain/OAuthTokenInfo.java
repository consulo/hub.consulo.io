package consulo.hub.shared.auth.oauth2.domain;

/**
 * @author VISTALL
 * @since 29/08/2021
 */
public class OAuthTokenInfo
{
	private String token;

	public OAuthTokenInfo()
	{
	}

	public OAuthTokenInfo(String token)
	{
		this.token = token;
	}

	public String getToken()
	{
		return token;
	}

	public void setToken(String token)
	{
		this.token = token;
	}
}
