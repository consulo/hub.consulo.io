package consulo.webService.errorReporter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import consulo.webService.errorReporter.domain.ErrorReport;
import consulo.webService.errorReporter.domain.ErrorReportAttachment;
import consulo.webService.errorReporter.mongo.ErrorReportAttachmentRepository;
import consulo.webService.errorReporter.mongo.ErrorReportRepository;

/**
 * @author VISTALL
 * @since 27-Sep-16
 */
@RestController
public class ErrorReportRestController
{
	@Autowired
	private ErrorReportRepository myErrorReportRepository;

	@Autowired
	private ErrorReportAttachmentRepository myErrorReportAttachmentRepository;

	@RequestMapping(value = "/api/errorReporter/create", method = RequestMethod.POST)
	public Map<String, String> create(@RequestBody ErrorReport errorReport) throws IOException
	{
		errorReport.setReporterEmail("vistall.valeriy@gmail.com");
		errorReport = myErrorReportRepository.save(errorReport);
		for(ErrorReportAttachment attachment : errorReport.getAttachments())
		{
			myErrorReportAttachmentRepository.save(attachment);
		}

		Map<String, String> map = new HashMap<>(1);
		map.put("id", errorReport.getId());
		return map;
	}
}
