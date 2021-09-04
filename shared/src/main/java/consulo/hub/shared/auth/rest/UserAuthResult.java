package consulo.hub.shared.auth.rest;

import consulo.hub.shared.auth.domain.UserAccount;

/**
 * @author VISTALL
 * @since 04/09/2021
 */
public class UserAuthResult
{
	private UserAccount myAccount;

	private String myToken;

	public UserAuthResult()
	{
	}

	public UserAuthResult(UserAccount account, String token)
	{
		myAccount = account;
		myToken = token;
	}

	public UserAccount getAccount()
	{
		return myAccount;
	}

	public void setAccount(UserAccount account)
	{
		myAccount = account;
	}

	public String getToken()
	{
		return myToken;
	}

	public void setToken(String token)
	{
		myToken = token;
	}

	@Override
	public String toString()
	{
		return "UserAuthResult{" +
				"myAccount=" + myAccount +
				", myToken='" + myToken + '\'' +
				'}';
	}
}
