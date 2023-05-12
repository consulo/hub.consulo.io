package consulo.procoeton.core.auth.backend;

import consulo.hub.shared.auth.domain.UserAccount;
import consulo.hub.shared.auth.oauth2.domain.SessionInfo;
import consulo.procoeton.core.backend.ApiBackendRequestor;
import consulo.procoeton.core.backend.BackendApiUrl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

/**
 * @author VISTALL
 * @since 04/05/2023
 */
public class BackendUserAccountServiceCore
{
	private Logger LOG = LoggerFactory.getLogger(getClass());

	@Autowired
	private ApiBackendRequestor myApiBackendRequestor;

	public SessionInfo revokeSessionByKey(UserAccount account, String token)
	{
		try
		{
			Map<String, String> map = new HashMap<>();
			map.put("token", token);

			return myApiBackendRequestor.runRequest(BackendApiUrl.toPrivate("/user/oauth/revoke/token"), map, SessionInfo.class);
		}
		catch(Exception e)
		{
			LOG.warn("Failed to revoke token: " + account.getId(), e);
		}
		return null;
	}

	public SessionInfo revokeSessionById(UserAccount account, String tokenId)
	{
		try
		{
			Map<String, String> map = new HashMap<>();
			map.put("tokenId", tokenId);

			return myApiBackendRequestor.runRequest(BackendApiUrl.toPrivate("/user/oauth/revoke/id"), map, SessionInfo.class);
		}
		catch(Exception e)
		{
			LOG.warn("Failed to revoke token: " + account.getId(), e);
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

			UserAccount account = myApiBackendRequestor.runRequest(BackendApiUrl.toPrivate("/user/changePassword"), map, UserAccount.class);
			return account != null;
		}
		catch(Exception e)
		{
			LOG.warn("Failed to changePassword: " + userId, e);
			return false;
		}
	}
}
