package consulo.hub.backend.frontend;

import consulo.hub.backend.auth.repository.UserAccountRepository;
import consulo.hub.backend.errorReporter.repository.ErrorReportRepository;
import consulo.hub.shared.auth.domain.UserAccount;
import consulo.hub.shared.errorReporter.domain.ErrorReport;
import consulo.hub.shared.errorReporter.domain.ErrorReportStatus;
import consulo.hub.shared.util.JsonPage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author VISTALL
 * @since 29/08/2021
 */
@RestController
public class FrontendErrorReporterRestController
{
	@Autowired
	private ErrorReportRepository myErrorReportRepository;

	@Autowired
	private UserAccountRepository myUserAccountRepository;

	@RequestMapping("/api/private/errorReport/info")
	public ErrorReport errorReportDirect(@RequestParam("longId") String longId)
	{
		return Objects.requireNonNull(myErrorReportRepository.findByLongId(longId));
	}

	@RequestMapping("/api/private/errorReport/changeStatus")
	public ErrorReport errorReportChangeStatus(@RequestParam("id") long errorReportId, @RequestParam(value = "userId") long userId, @RequestParam("status") ErrorReportStatus status)
	{
		UserAccount reportUser = Objects.requireNonNull(myUserAccountRepository.findOne(userId));

		ErrorReport errorReport = Objects.requireNonNull(myErrorReportRepository.findOne(errorReportId));

		errorReport.setStatus(status);
		errorReport.setChangedByUser(reportUser);
		errorReport.setChangeTime(System.currentTimeMillis());

		return myErrorReportRepository.save(errorReport);
	}

	@RequestMapping("/api/private/errorReport/list")
	public JsonPage<ErrorReport> listErrorReports(@RequestParam(value = "userId", defaultValue = "0", required = false) long userId,
												  @RequestParam(value = "statuses", required = false) String statuses,
												  @RequestParam("pageSize") int pageSize,
												  @RequestParam("page") int page)
	{
		UserAccount reportUser = null;
		if(userId != 0)
		{
			reportUser = Objects.requireNonNull(myUserAccountRepository.findOne(userId));
		}

		ErrorReportStatus[] selectStatuses = ErrorReportStatus.values();

		if(!StringUtils.isBlank(statuses))
		{
			selectStatuses = Arrays.stream(statuses.split(",")).map(ErrorReportStatus::valueOf).toArray(ErrorReportStatus[]::new);
		}

		Page<ErrorReport> reports;

		if(reportUser != null)
		{
			reports = myErrorReportRepository.findByUserAndStatusIn(reportUser, selectStatuses, new PageRequest(page, pageSize, new Sort(Sort.Direction.ASC, "createDate")));
		}
		else
		{
			reports = myErrorReportRepository.findByStatusIn(selectStatuses, new PageRequest(page, pageSize, new Sort(Sort.Direction.ASC, "createDate")));
		}

		return new JsonPage<>(reports);
	}
}
