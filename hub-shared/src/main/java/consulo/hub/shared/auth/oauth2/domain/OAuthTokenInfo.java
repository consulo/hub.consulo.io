package consulo.hub.shared.auth.oauth2.domain;

import java.util.Map;

/**
 * @author VISTALL
 * @since 29/08/2021
 */
public class OAuthTokenInfo
{
	private String myToken;
	private Map<String, Object> myAdditionalInfo;

	public OAuthTokenInfo()
	{
	}

	public OAuthTokenInfo(String token, Map<String, Object> additionalInfo)
	{
		myToken = token;
		myAdditionalInfo = additionalInfo;
	}

	public String getToken()
	{
		return myToken;
	}

	public void setToken(String token)
	{
		this.myToken = token;
	}

	public Map<String, Object> getAdditionalInfo()
	{
		return myAdditionalInfo;
	}

	public void setAdditionalInfo(Map<String, Object> additionalInfo)
	{
		myAdditionalInfo = additionalInfo;
	}
}
