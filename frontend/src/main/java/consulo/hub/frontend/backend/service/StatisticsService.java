package consulo.hub.frontend.backend.service;

import consulo.hub.shared.statistics.domain.MongoStatisticBean;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author VISTALL
 * @since 21/08/2021
 */
@Service
public class StatisticsService
{
	public List<MongoStatisticBean> findAll(Sort sort)
	{
		throw new UnsupportedOperationException();
	}
}
