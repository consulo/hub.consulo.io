package consulo.procoeton.core.backend;

/**
 * @author VISTALL
 * @since 05/05/2023
 */
public class BackendApiUrl
{
	public static BackendApiUrl toPrivate(String api)
	{
		return new BackendApiUrl(api, true);
	}

	public static BackendApiUrl toPublic(String api)
	{
		return new BackendApiUrl(api, false);
	}

	private final String myApiPrefix;
	private final boolean myIsPrivate;

	private BackendApiUrl(String apiPrefix, boolean isPrivate)
	{
		myApiPrefix = apiPrefix;
		myIsPrivate = isPrivate;
	}

	public boolean isPrivate()
	{
		return myIsPrivate;
	}

	public String build(String hostname)
	{
		if(myIsPrivate)
		{
			return hostname + "/api/private" + myApiPrefix;
		}
		else
		{
			return hostname + "/api" + myApiPrefix;
		}
	}
}
