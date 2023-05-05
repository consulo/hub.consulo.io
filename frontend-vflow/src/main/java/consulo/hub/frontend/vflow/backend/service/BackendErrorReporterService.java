package consulo.hub.frontend.vflow.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import consulo.hub.shared.errorReporter.domain.ErrorReport;
import consulo.hub.shared.errorReporter.domain.ErrorReportStatus;
import consulo.hub.shared.util.JsonPage;
import consulo.procoeton.core.backend.ApiBackendRequestor;
import consulo.procoeton.core.backend.BackendApiUrl;
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
	private ApiBackendRequestor myApiBackendRequestor;

	public ErrorReport changeStatus(long errorReportId, ErrorReportStatus status, long byUserId)
	{
		try
		{
			Map<String, String> params = new HashMap<>();
			params.put("userId", String.valueOf(byUserId));
			params.put("status", status.name());
			params.put("id", String.valueOf(errorReportId));

			return myApiBackendRequestor.runRequest(BackendApiUrl.toPrivate("/errorReporter/changeStatus"), params, ErrorReport.class);
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
		if(userId > 0)
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
			String api = "/errorReporter/list";
			BackendApiUrl backendApiUrl;
			if(userId == -1)
			{
				backendApiUrl = BackendApiUrl.toPublic(api);
			}
			else
			{
				backendApiUrl = BackendApiUrl.toPrivate(api);
			}

			JsonPage<ErrorReport> jsonPage = myApiBackendRequestor.runRequest(backendApiUrl, params, new TypeReference<JsonPage<ErrorReport>>()
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
			return myApiBackendRequestor.runRequest(BackendApiUrl.toPrivate("/errorReporter/info"), Map.<String, String>of("longId", errorReportId), ErrorReport.class);
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}
