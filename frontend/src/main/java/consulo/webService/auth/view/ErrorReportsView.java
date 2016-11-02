package consulo.webService.auth.view;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import com.vaadin.spring.annotation.SpringView;
import consulo.webService.errorReporter.domain.ErrorReport;
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
	protected List<ErrorReport> getReports(Authentication authentication)
	{
		return myErrorReportRepository.findByReporterEmail(authentication.getName(), new Sort(Sort.Direction.DESC, ErrorReportRepository.CREATE_DATE));
	}
}
