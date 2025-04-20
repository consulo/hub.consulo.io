package consulo.hub.frontend.vflow.statistics.view;

import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import consulo.hub.shared.auth.Roles;
import consulo.hub.shared.statistics.domain.StatisticEntry;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;

/**
 * @author VISTALL
 * @since 2020-05-31
 */
@PageTitle("Admin/Statistics")
@RolesAllowed(Roles.ROLE_SUPERUSER)
@Route("admin/statistics")
public class AdminStatisticsView extends BaseStatisticsView {
    @Override
    protected List<StatisticEntry> getStatistics() {
        return myStatisticRepository.listAll(0);
    }
}
