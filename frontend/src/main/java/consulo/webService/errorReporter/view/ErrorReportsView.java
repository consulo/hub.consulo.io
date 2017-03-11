package consulo.webService.errorReporter.view;

import java.util.Collections;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.vaadin.spring.annotation.SpringView;
import consulo.webService.errorReporter.domain.ErrorReport;
import consulo.webService.errorReporter.domain.ErrorReporterStatus;
import consulo.webService.errorReporter.mongo.ErrorReportRepository;

/**
 * @author VISTALL
 * @since 02-Oct-16
 */
@SpringView(name = ErrorReportsView.ID)
public class ErrorReportsView extends BaseErrorReportsView
{
	public static final String ID = "errorReports";

	@Override
	protected Page<ErrorReport> getReports(int page, ErrorReporterStatus[] errorReporterStatuses, int pageSize)
	{
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if(authentication == null)
		{
			return new PageImpl<>(Collections.emptyList());
		}

		return myErrorReportRepository.findByReporterEmailAndStatusIn(authentication.getName(), errorReporterStatuses, new PageRequest(page, pageSize, new Sort(Sort.Direction.DESC, ErrorReportRepository.CREATE_DATE)));
	}
}
