package consulo.hub.backend.statistics;

import consulo.hub.backend.statistics.mongo.StatisticRepository;
import consulo.hub.shared.ServiceAccounts;
import consulo.hub.shared.auth.domain.UserAccount;
import consulo.hub.shared.statistics.domain.StatisticBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author VISTALL
 * @since 2020-05-31
 */
@RestController
public class StatisticsRestController
{
	private static enum PushResult
	{
		OK
	}

	@Autowired
	private StatisticRepository myStatisticRepository;

	@RequestMapping(value = "/api/statistics/push", method = RequestMethod.POST)
	public Map<String, String> doPushStatistic(@AuthenticationPrincipal UserAccount account, @RequestBody StatisticBean bean)
	{
		String ownerEmail = account != null ? account.getUsername() : ServiceAccounts.STATISTICS;

		bean.setOwnerEmail(ownerEmail);

		bean.setCreateTime(System.currentTimeMillis());

		myStatisticRepository.save(bean);

		return resultWithMessage(PushResult.OK, null);
	}

	private static Map<String, String> resultWithMessage(PushResult result, String message)
	{
		Map<String, String> map = new HashMap<>(1);
		map.put("type", result.name());
		if(message != null)
		{
			map.put("message", message);
		}
		return map;
	}
}
