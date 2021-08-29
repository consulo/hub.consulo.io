package consulo.hub.frontend.errorReporter.view;

import com.vaadin.spring.annotation.SpringView;
import consulo.hub.frontend.backend.service.BackendErrorReporterService;
import consulo.hub.shared.auth.SecurityUtil;
import consulo.hub.shared.auth.domain.UserAccount;
import consulo.hub.shared.errorReporter.domain.ErrorReport;
import consulo.hub.shared.errorReporter.domain.ErrorReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Collections;

/**
 * @author VISTALL
 * @since 02-Oct-16
 */
@SpringView(name = ErrorReportsView.ID)
public class ErrorReportsView extends BaseErrorReportsView
{
	public static final String ID = "errorReports";

	@Override
	protected Page<ErrorReport> getReports(int page, ErrorReportStatus[] errorReportStatuses, int pageSize)
	{
		UserAccount userAccout = SecurityUtil.getUserAccout();
		if(userAccout == null)
		{
			return new PageImpl<>(Collections.emptyList());
		}

		return myErrorReportRepository.findByUserAndStatuses(userAccout.getId(), errorReportStatuses, new PageRequest(page, pageSize, new Sort(Sort.Direction.DESC,
				BackendErrorReporterService.CREATE_DATE)));
	}
}
