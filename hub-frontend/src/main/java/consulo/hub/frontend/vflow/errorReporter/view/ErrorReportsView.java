package consulo.hub.frontend.vflow.errorReporter.view;

import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import consulo.hub.frontend.vflow.backend.service.BackendErrorReporterService;
import consulo.hub.frontend.vflow.base.MainLayout;
import consulo.hub.shared.auth.SecurityUtil;
import consulo.hub.shared.auth.domain.UserAccount;
import consulo.hub.shared.errorReporter.domain.ErrorReport;
import consulo.hub.shared.errorReporter.domain.ErrorReportStatus;
import jakarta.annotation.security.PermitAll;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Collections;

/**
 * @author VISTALL
 * @since 2016-10-02
 */
@PageTitle("Error Reports")
@Route(value = "user/errorReports", layout = MainLayout.class)
@PermitAll
public class ErrorReportsView extends BaseErrorReportsView {
    public static final String ID = "errorReports";

    @Override
    protected Page<ErrorReport> getReports(int page, ErrorReportStatus[] errorReportStatuses, int pageSize) {
        UserAccount userAccout = SecurityUtil.getUserAccout();
        if (userAccout == null) {
            return new PageImpl<>(Collections.emptyList());
        }

        // -1 - invoke public api
        return myErrorReportRepository.findByUserAndStatuses(
            -1,
            errorReportStatuses,
            PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, BackendErrorReporterService.CREATE_DATE))
        );
    }
}
