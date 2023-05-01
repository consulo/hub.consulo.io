package consulo.hub.frontend.vflow.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import consulo.hub.frontend.vflow.backend.BackendRequestor;
import consulo.hub.shared.auth.domain.UserAccount;
import consulo.hub.shared.auth.oauth2.domain.OAuthTokenInfo;
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
public class BackendUserAccountService
{
	private static final Logger LOG = LoggerFactory.getLogger(BackendUserAccountService.class);

	@Autowired
	private BackendRequestor myBackendRequestor;

	public boolean registerUser(String userName, String password)
	{
		try
		{
			Map<String, String> map = new HashMap<>();
			map.put("email", userName);
			map.put("password", password);

			UserAccount account = myBackendRequestor.runRequest("/user/register", map, UserAccount.class);
			return account != null;
		}
		catch(Exception e)
		{
			LOG.warn("Failed to register: " + userName, e);
		}
		return false;
	}

	public Map<String, String> requestOAuthKey(UserAccount account, String token, String hostName)
	{
		try
		{
			Map<String, String> map = new HashMap<>();
			map.put("userId", String.valueOf(account.getId()));
			map.put("token", token);
			map.put("hostName", hostName);

			return myBackendRequestor.runRequest("/user/oauth/request", map, new TypeReference<Map<String, String>>()
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

			OAuthTokenInfo[] oAuthTokenInfos = myBackendRequestor.runRequest("/user/oauth/list", map, OAuthTokenInfo[].class);
			if(oAuthTokenInfos == null)
			{
				OAuthTokenInfo oAuthTokenInfo = new OAuthTokenInfo();
				oAuthTokenInfo.setToken("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
				oAuthTokenInfos = new OAuthTokenInfo[]{
						oAuthTokenInfo
				};
			}
			return oAuthTokenInfos;
		}
		catch(Exception e)
		{
			LOG.warn("Failed to list tokens: " + account.getId(), e);
		}
		return new OAuthTokenInfo[0];
	}

	public OAuthTokenInfo addOAuthToken(UserAccount account, String name)
	{
		try
		{
			Map<String, String> map = new HashMap<>();
			map.put("userId", String.valueOf(account.getId()));
			map.put("name", name);

			return myBackendRequestor.runRequest("/user/oauth/add", map, OAuthTokenInfo.class);
		}
		catch(Exception e)
		{
			LOG.warn("Failed to add token: " + account.getId(), e);
		}
		return null;
	}

	public OAuthTokenInfo removeOAuthToken(UserAccount account, String token)
	{
		try
		{
			Map<String, String> map = new HashMap<>();
			map.put("userId", String.valueOf(account.getId()));
			map.put("token", token);

			return myBackendRequestor.runRequest("/user/oauth/remove", map, OAuthTokenInfo.class);
		}
		catch(Exception e)
		{
			LOG.warn("Failed to add token: " + account.getId(), e);
		}
		return null;
	}

	public boolean changePassword(long userId, String oldPassword, String newPassword)
	{
		try
		{
			Map<String, String> map = new HashMap<>();
			map.put("userId", String.valueOf(userId));
			map.put("oldPassword", oldPassword);
			map.put("newPassword", newPassword);

			UserAccount account = myBackendRequestor.runRequest("/user/changePassword", map, UserAccount.class);
			return account != null;
		}
		catch(Exception e)
		{
			LOG.warn("Failed to changePassword: " + userId, e);
			return false;
		}
	}

	public List<UserAccount> listAll()
	{
		try
		{
			return List.of(myBackendRequestor.runRequest("/user/list", Map.of(), UserAccount[].class, () -> new UserAccount[0]));
		}
		catch(Exception e)
		{
			LOG.warn("Failed to list all users", e);
			return List.of();
		}
	}
}
