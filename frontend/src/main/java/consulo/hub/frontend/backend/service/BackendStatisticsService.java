package consulo.hub.frontend.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import consulo.hub.frontend.backend.BackendRequestor;
import consulo.hub.shared.statistics.domain.StatisticEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author VISTALL
 * @since 21/08/2021
 */
@Service
public class BackendStatisticsService
{
	private static final Logger LOG = LoggerFactory.getLogger(BackendStatisticsService.class);

	@Autowired
	private BackendRequestor myBackendRequestor;

	public List<StatisticEntry> listAll(long userId)
	{
		try
		{
			Map<String, String> args = Map.of();
			if(userId != 0)
			{
				args = Map.of("userId", String.valueOf(userId));
			}
			return myBackendRequestor.runRequest("/statistics/list", args, new TypeReference<List<StatisticEntry>>()
			{
			});
		}
		catch(Exception e)
		{
			LOG.error(e.getMessage(), e);
			return List.of();
		}
	}
}
