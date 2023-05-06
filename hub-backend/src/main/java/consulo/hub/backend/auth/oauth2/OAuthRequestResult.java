package consulo.hub.backend.auth.oauth2;

import consulo.hub.shared.auth.domain.UserAccount;

/**
 * @author VISTALL
 * @since 04/09/2021
 */
public class OAuthRequestResult
{
	private UserAccount myUserAccount;
	private String myToken;

	public OAuthRequestResult(String token, UserAccount userAccount)
	{
		myToken = token;
		myUserAccount = userAccount;
	}

	public UserAccount getUserAccount()
	{
		return myUserAccount;
	}

	public void setUserAccount(UserAccount userAccount)
	{
		myUserAccount = userAccount;
	}

	public String getToken()
	{
		return myToken;
	}

	public void setToken(String token)
	{
		myToken = token;
	}
}
