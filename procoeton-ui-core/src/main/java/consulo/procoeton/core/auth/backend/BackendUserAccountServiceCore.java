package consulo.procoeton.core.auth.backend;

import consulo.hub.shared.auth.domain.UserAccount;
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
