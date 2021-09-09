package consulo.hub.frontend.statistics.view;

import com.vaadin.spring.annotation.SpringView;
import consulo.hub.shared.auth.SecurityUtil;
import consulo.hub.shared.statistics.domain.StatisticEntry;

import java.util.List;

/**
 * @author VISTALL
 * @since 09/09/2021
 */
@SpringView(name = StatisticsView.ID)
public class StatisticsView extends BaseStatisticsView
{
	public static final String ID = "statistics";

	@Override
	protected List<StatisticEntry> getStatistics()
	{
		return myStatisticRepository.listAll(SecurityUtil.getUserAccout().getId());
	}
}
