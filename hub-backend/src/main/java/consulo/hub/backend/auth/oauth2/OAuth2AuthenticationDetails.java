package consulo.hub.backend.auth.oauth2;

import consulo.hub.shared.auth.HubClaimNames;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

import java.util.Objects;

/**
 * @author VISTALL
 * @since 09/05/2023
 */
public class OAuth2AuthenticationDetails extends WebAuthenticationDetails
{
	private final String clientName;
	private final String subClientName;

	public OAuth2AuthenticationDetails(HttpServletRequest request)
	{
		super(request);

		clientName = request.getParameter(HubClaimNames.CLIENT_NAME);
		subClientName = request.getParameter(HubClaimNames.SUB_CLIENT_NAME);
	}

	public OAuth2AuthenticationDetails(String remoteAddress, String sessionId, String clientName, String subClientName)
	{
		super(remoteAddress, sessionId);
		this.clientName = clientName;
		this.subClientName = subClientName;
	}

	public String getClientName()
	{
		return clientName;
	}

	public String getSubClientName()
	{
		return subClientName;
	}

	@Override
	public boolean equals(Object o)
	{
		if(this == o)
		{
			return true;
		}
		if(o == null || getClass() != o.getClass())
		{
			return false;
		}
		if(!super.equals(o))
		{
			return false;
		}
		OAuth2AuthenticationDetails that = (OAuth2AuthenticationDetails) o;
		return Objects.equals(clientName, that.clientName) &&
				Objects.equals(subClientName, that.subClientName);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(super.hashCode(), clientName, subClientName);
	}

	@Override
	public String toString()
	{
		return "OAuth2AuthenticationDetails{" +
				"clientName='" + clientName + '\'' +
				", subClientName='" + subClientName + '\'' +
				"} " + super.toString();
	}
}
