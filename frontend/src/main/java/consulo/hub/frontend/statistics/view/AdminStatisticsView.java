package consulo.hub.frontend.statistics.view;

import com.vaadin.spring.annotation.SpringView;
import consulo.hub.shared.statistics.domain.StatisticEntry;

import java.util.List;

/**
 * @author VISTALL
 * @since 2020-05-31
 */
@SpringView(name = AdminStatisticsView.ID)
public class AdminStatisticsView extends BaseStatisticsView
{
	public static final String ID = "adminStatistics";

	@Override
	protected List<StatisticEntry> getStatistics()
	{
		return myStatisticRepository.listAll(0);
	}
}
