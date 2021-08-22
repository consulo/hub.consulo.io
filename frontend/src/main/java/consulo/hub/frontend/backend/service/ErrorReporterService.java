package consulo.hub.frontend.backend.service;

import consulo.hub.shared.errorReporter.domain.ErrorReport;
import consulo.hub.shared.errorReporter.domain.ErrorReporterStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

/**
 * @author VISTALL
 * @since 21/08/2021
 */
@Service
public class ErrorReporterService
{
	public static String CREATE_DATE = "createDate";

	public ErrorReport changeStatus(String errorReportId, ErrorReporterStatus status, int byUserId)
	{
		throw new UnsupportedOperationException();
	}

	public Page<ErrorReport> findByReporterEmail(String email, PageRequest pageRequest)
	{
		throw new UnsupportedOperationException();
	}

	public Page<ErrorReport> findByStatusIn(ErrorReporterStatus[] errorReporterStatuses, PageRequest pageRequest)
	{
		throw new UnsupportedOperationException();
	}

	public Page<ErrorReport> findByReporterEmailAndStatusIn(String email, ErrorReporterStatus[] errorReporterStatuses, PageRequest pageRequest)
	{
		throw new UnsupportedOperationException();
	}

	public ErrorReport findOne(String errorReportId)
	{
		throw new UnsupportedOperationException();
	}
}
