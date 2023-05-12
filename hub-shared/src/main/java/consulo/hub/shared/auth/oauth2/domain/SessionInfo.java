package consulo.hub.shared.auth.oauth2.domain;

import java.util.Map;

/**
 * @author VISTALL
 * @since 29/08/2021
 */
public class SessionInfo
{
	private String myId;
	private Map<String, Object> myAdditionalInfo;

	public SessionInfo()
	{
	}

	public SessionInfo(String id, Map<String, Object> additionalInfo)
	{
		myId = id;
		myAdditionalInfo = additionalInfo;
	}

	public String getId()
	{
		return myId;
	}

	public void setId(String id)
	{
		this.myId = id;
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
