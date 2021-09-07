package consulo.hub.backend.frontend;

import consulo.hub.backend.statistics.repository.StatisticEntryRepository;
import consulo.hub.shared.statistics.domain.StatisticEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author VISTALL
 * @since 07/09/2021
 */
@RestController
public class FrontendStatisticsRestController
{
	@Autowired
	private StatisticEntryRepository myStatisticEntryRepository;

	@RequestMapping("/api/private/statistics/list")
	public List<StatisticEntry> listStatistics()
	{
		return myStatisticEntryRepository.findAll(new Sort(Sort.Direction.ASC, "createTime"));
	}
}
