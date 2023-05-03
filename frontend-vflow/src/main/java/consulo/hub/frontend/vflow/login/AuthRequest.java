package consulo.hub.frontend.vflow.login;

/**
* @author VISTALL
* @since 02/05/2023
*/
public class AuthRequest
{
	public String email;

	public String password;

	public String getEmail()
	{
		return email;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}
}
