package consulo.hub.frontend.vflow.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import consulo.hub.frontend.vflow.backend.BackendRequestor;
import consulo.hub.shared.errorReporter.domain.ErrorReport;
import consulo.hub.shared.errorReporter.domain.ErrorReportStatus;
import consulo.hub.shared.util.JsonPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author VISTALL
 * @since 21/08/2021
 */
@Service
public class BackendErrorReporterService
{
	public static String CREATE_DATE = "createDate";

	@Autowired
	private BackendRequestor myBackendRequestor;

	public ErrorReport changeStatus(long errorReportId, ErrorReportStatus status, long byUserId)
	{
		try
		{
			Map<String, String> params = new HashMap<>();
			params.put("userId", String.valueOf(byUserId));
			params.put("status", status.name());
			params.put("id", String.valueOf(errorReportId));

			return myBackendRequestor.runRequest("/errorReport/changeStatus", params, ErrorReport.class);
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public Page<ErrorReport> findByUser(long userId, PageRequest pageRequest)
	{
		return findAll(userId, new ErrorReportStatus[0], pageRequest);
	}

	public Page<ErrorReport> findByStatuses(ErrorReportStatus[] errorReportStatuses, PageRequest pageRequest)
	{
		return findAll(0, errorReportStatuses, pageRequest);
	}

	public Page<ErrorReport> findByUserAndStatuses(long userId, ErrorReportStatus[] errorReportStatuses, PageRequest pageRequest)
	{
		return findAll(userId, errorReportStatuses, pageRequest);
	}

	private Page<ErrorReport> findAll(long userId, ErrorReportStatus[] errorReportStatuses, PageRequest pageRequest)
	{
		Map<String, String> params = new HashMap<>();
		if(userId != 0)
		{
			params.put("userId", String.valueOf(userId));
		}

		if(errorReportStatuses.length != 0)
		{
			params.put("statuses", Arrays.stream(errorReportStatuses).map(ErrorReportStatus::name).collect(Collectors.joining(",")));
		}

		params.put("pageSize", String.valueOf(pageRequest.getPageSize()));
		params.put("page", String.valueOf(pageRequest.getPageNumber()));

		try
		{
			JsonPage<ErrorReport> jsonPage = myBackendRequestor.runRequest("/errorReport/list", params, new TypeReference<JsonPage<ErrorReport>>()
			{
			});

			if(jsonPage == null)
			{
				return Page.empty();
			}

			return JsonPage.from(jsonPage, pageRequest.getPageSize());
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
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
