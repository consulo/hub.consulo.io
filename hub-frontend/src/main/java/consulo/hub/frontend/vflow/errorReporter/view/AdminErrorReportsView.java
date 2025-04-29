package consulo.hub.frontend.vflow.errorReporter.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import consulo.hub.frontend.vflow.backend.service.BackendErrorReporterService;
import consulo.hub.frontend.vflow.base.MainLayout;
import consulo.procoeton.core.vaadin.ui.util.VaadinUIUtil;
import consulo.procoeton.core.util.AuthUtil;
import consulo.hub.shared.auth.Roles;
import consulo.hub.shared.errorReporter.domain.ErrorReport;
import consulo.hub.shared.errorReporter.domain.ErrorReportStatus;
import consulo.util.lang.StringUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 2016-11-02
 */
@PageTitle("Admin/Error Reports")
@Route(value = "admin/errorReports", layout = MainLayout.class)
@RolesAllowed(Roles.ROLE_SUPERUSER)
public class AdminErrorReportsView extends BaseErrorReportsView {
    @Nonnull
    @Override
    protected ErrorReportComponent createErrorReportComponent(ErrorReport errorReport) {
        return new ErrorReportComponent(errorReport) {
            @Override
            protected void addRightButtons(
                ErrorReport errorReport,
                VerticalLayout lineLayout,
                HorizontalLayout rightLayout,
                List<Consumer<ErrorReport>> onUpdate
            ) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication == null) {
                    super.addRightButtons(errorReport, lineLayout, rightLayout, onUpdate);
                    return;
                }

                HorizontalLayout panel = VaadinUIUtil.newHorizontalLayout();
                panel.setSpacing(true);
                rightLayout.add(panel);

                Map<ErrorReportStatus, Button> adminButtons = new LinkedHashMap<>();
                for (ErrorReportStatus status : ErrorReportStatus.values()) {
                    String captalizedStatus = StringUtil.capitalize(status.name().toLowerCase(Locale.US));
                    Button button = new Button(captalizedStatus);
                    //button.addStyleName("errorViewButton" + captalizedStatus);

                    adminButtons.put(status, button);

                    button.addClickListener(e -> {
                        if (errorReport.getStatus() != status) {
                            fireChanged(onUpdate, errorReport);

                            ErrorReport updated = myErrorReportRepository.changeStatus(errorReport.getId(), status, AuthUtil.getUserId());
                            if (updated != null) {
                                fireChanged(onUpdate, updated);
                            }
                        }
                    });
                }

                onUpdate.add(report -> {
                    panel.removeAll();

                    for (ErrorReportStatus errorReportStatus : ErrorReportStatus.values()) {
                        if (errorReportStatus == report.getStatus()) {
                            continue;
                        }

                        Button button = adminButtons.get(errorReportStatus);
                        panel.add(button);
                    }

                    // hide from view
                    if (!myFilters.contains(report.getStatus())) {
                        myReportList.removeItem(lineLayout);
                        myLastPageSize--;
                        updateHeader();
                    }
                });

                super.addRightButtons(errorReport, lineLayout, rightLayout, onUpdate);
            }
        };
    }

    @Override
    protected Page<ErrorReport> getReports(int page, ErrorReportStatus[] errorReportStatuses, int pageSize) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return new PageImpl<>(Collections.emptyList());
        }

        return myErrorReportRepository.findByStatuses(
            errorReportStatuses,
            PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, BackendErrorReporterService.CREATE_DATE))
        );
    }
}
