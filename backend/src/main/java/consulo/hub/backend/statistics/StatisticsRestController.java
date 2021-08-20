package consulo.hub.backend.statistics;

import consulo.hub.backend.auth.oauth2.domain.OAuth2AuthenticationAccessToken;
import consulo.hub.backend.auth.oauth2.mongo.OAuth2AccessTokenRepository;
import consulo.hub.shared.statistics.domain.StatisticBean;
import consulo.hub.backend.statistics.mongo.StatisticRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
		OK,
		BAD_OAUTHK_KEY
	}

	@Autowired
	private StatisticRepository myStatisticRepository;

	@Autowired
	private OAuth2AccessTokenRepository myOAuth2AccessTokenRepository;

	@RequestMapping(value = "/api/statistics/push", method = RequestMethod.POST)
	public Map<String, String> doPushStatistic(@RequestHeader(value = "Authorization", required = false) String authorizationKey, @RequestBody StatisticBean bean)
	{
		String ownerEmail = null;
		if(authorizationKey != null)
		{
			OAuth2AuthenticationAccessToken token = myOAuth2AccessTokenRepository.findByTokenId(authorizationKey);
			if(token == null)
			{
				return resultWithMessage(PushResult.BAD_OAUTHK_KEY, null);
			}

			ownerEmail = token.getUserName();
		}

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
