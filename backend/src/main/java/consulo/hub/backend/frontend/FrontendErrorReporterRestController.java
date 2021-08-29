package consulo.hub.backend.frontend;

import consulo.hub.backend.errorReporter.repository.ErrorReportRepository;
import consulo.hub.shared.errorReporter.domain.ErrorReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author VISTALL
 * @since 29/08/2021
 */
@RestController
public class FrontendErrorReporterRestController
{
	@Autowired
	private ErrorReportRepository myErrorReportRepository;

	@RequestMapping("/api/private/errorReport/info")
	public ErrorReport errorReportDirect(@RequestParam("longId") String longId)
	{
		return myErrorReportRepository.findByLongId(longId);
	}
}
