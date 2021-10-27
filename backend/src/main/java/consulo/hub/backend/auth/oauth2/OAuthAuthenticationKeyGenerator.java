package consulo.hub.backend.auth.oauth2;

import org.springframework.security.oauth2.common.util.OAuth2Utils;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.AuthenticationKeyGenerator;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;

/**
 * @author VISTALL
 * @since 27/10/2021
 *
 * @see org.springframework.security.oauth2.provider.token.DefaultAuthenticationKeyGenerator
 */
public class OAuthAuthenticationKeyGenerator implements AuthenticationKeyGenerator
{
	private static final String CLIENT_ID = "client_id";

	private static final String SCOPE = "scope";

	private static final String USERNAME = "username";

	@Override
	public String extractKey(OAuth2Authentication authentication)
	{
		Map<String, String> values = new LinkedHashMap<>();
		OAuth2Request authorizationRequest = authentication.getOAuth2Request();
		if(!authentication.isClientOnly())
		{
			values.put(USERNAME, authentication.getName());
		}
		values.put(CLIENT_ID, authorizationRequest.getClientId());
		if(authorizationRequest.getScope() != null)
		{
			values.put(SCOPE, OAuth2Utils.formatParameterList(new TreeSet<>(authorizationRequest.getScope())));
		}
		// include extensions too. because DefaultAuthenticationKeyGenerator not hold it
		for(Map.Entry<String, Serializable> entry : authorizationRequest.getExtensions().entrySet())
		{
			values.put(entry.getKey(), String.valueOf(entry.getValue()));
		}
		return generateKey(values);
	}

	protected String generateKey(Map<String, String> values)
	{
		MessageDigest digest;
		try
		{
			digest = MessageDigest.getInstance("MD5");
			byte[] bytes = digest.digest(values.toString().getBytes("UTF-8"));
			return String.format("%032x", new BigInteger(1, bytes));
		}
		catch(NoSuchAlgorithmException nsae)
		{
			throw new IllegalStateException("MD5 algorithm not available.  Fatal (should be in the JDK).", nsae);
		}
		catch(UnsupportedEncodingException uee)
		{
			throw new IllegalStateException("UTF-8 encoding not available.  Fatal (should be in the JDK).", uee);
		}
	}
}
