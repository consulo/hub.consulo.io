package consulo.hub.frontend.backend.service;

import consulo.hub.frontend.backend.BackendRequestor;
import consulo.hub.shared.errorReporter.domain.ErrorReport;
import consulo.hub.shared.errorReporter.domain.ErrorReportStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author VISTALL
 * @since 21/08/2021
 */
@Service
public class ErrorReporterService
{
	public static String CREATE_DATE = "createDate";

	@Autowired
	private BackendRequestor myBackendRequestor;

	public ErrorReport changeStatus(long errorReportId, ErrorReportStatus status, long byUserId)
	{
		throw new UnsupportedOperationException();
	}

	public Page<ErrorReport> findByReporterEmail(long userId, PageRequest pageRequest)
	{
		throw new UnsupportedOperationException();
	}

	public Page<ErrorReport> findByStatusIn(ErrorReportStatus[] errorReportStatuses, PageRequest pageRequest)
	{
		throw new UnsupportedOperationException();
	}

	public Page<ErrorReport> findByReporterEmailAndStatusIn(long userId, ErrorReportStatus[] errorReportStatuses, PageRequest pageRequest)
	{
		throw new UnsupportedOperationException();
	}

	public ErrorReport findByLongId(String errorReportId)
	{
		try
		{
			return myBackendRequestor.runRequest("/errorReport/info", Map.<String, String>of("longId", errorReportId), ErrorReport.class);
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}
