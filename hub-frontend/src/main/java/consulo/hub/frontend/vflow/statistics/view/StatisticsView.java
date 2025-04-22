package consulo.hub.frontend.vflow.statistics.view;

import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import consulo.hub.shared.auth.SecurityUtil;
import consulo.hub.shared.statistics.domain.StatisticEntry;
import jakarta.annotation.security.PermitAll;

import java.util.List;

/**
 * @author VISTALL
 * @since 2021-09-09
 */
@PermitAll
@PageTitle("Statistics")
@Route("user/statistics")
public class StatisticsView extends BaseStatisticsView {
    @Override
    protected List<StatisticEntry> getStatistics() {
        return myStatisticRepository.listAll(SecurityUtil.getUserAccout().getId());
    }
}
