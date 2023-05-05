package consulo.hub.frontend.vflow.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import consulo.procoeton.core.auth.backend.BackendUserAccountServiceCore;
import consulo.procoeton.core.backend.ApiBackendRequestor;
import consulo.hub.shared.auth.domain.UserAccount;
import consulo.hub.shared.auth.oauth2.domain.OAuthTokenInfo;
import consulo.procoeton.core.backend.BackendApiUrl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author VISTALL
 * @since 21/08/2021
 */
@Service
public class BackendUserAccountService extends BackendUserAccountServiceCore
{
	private static final Logger LOG = LoggerFactory.getLogger(BackendUserAccountService.class);

	@Autowired
	private ApiBackendRequestor myApiBackendRequestor;

	public Map<String, String> requestOAuthKey(UserAccount account, String token, String hostName)
	{
		try
		{
			Map<String, String> map = new HashMap<>();
			map.put("userId", String.valueOf(account.getId()));
			map.put("token", token);
			map.put("hostName", hostName);

			return myApiBackendRequestor.runRequest(BackendApiUrl.toPrivate("/user/oauth/request"), map, new TypeReference<Map<String, String>>()
			{
			});
		}
		catch(Exception e)
		{
			LOG.warn("Failed to list tokens: " + account.getId(), e);
			return Map.of();
		}
	}

	public OAuthTokenInfo[] listOAuthTokens(UserAccount account)
	{
		try
		{
			Map<String, String> map = new HashMap<>();
			map.put("userId", String.valueOf(account.getId()));

			return myApiBackendRequestor.runRequest(BackendApiUrl.toPrivate("/user/oauth/list"), map, OAuthTokenInfo[].class);
		}
		catch(Exception e)
		{
			LOG.warn("Failed to list tokens: " + account.getId(), e);
		}
		return new OAuthTokenInfo[0];
	}

	public OAuthTokenInfo removeOAuthToken(UserAccount account, String token)
	{
		try
		{
			Map<String, String> map = new HashMap<>();
			map.put("userId", String.valueOf(account.getId()));
			map.put("token", token);

			return myApiBackendRequestor.runRequest(BackendApiUrl.toPrivate("/user/oauth/remove"), map, OAuthTokenInfo.class);
		}
		catch(Exception e)
		{
			LOG.warn("Failed to add token: " + account.getId(), e);
		}
		return null;
	}

	public List<UserAccount> listAll()
	{
		try
		{
			return List.of(myApiBackendRequestor.runRequest(BackendApiUrl.toPrivate("/user/list"), Map.of(), UserAccount[].class, () -> new UserAccount[0]));
		}
		catch(Exception e)
		{
			LOG.warn("Failed to list all users", e);
			return List.of();
		}
	}
}
