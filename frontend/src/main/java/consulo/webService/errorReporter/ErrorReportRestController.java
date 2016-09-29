package consulo.webService.errorReporter;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author VISTALL
 * @since 27-Sep-16
 */
@RestController
public class ErrorReportRestController
{
	@RequestMapping(value = "/api/errorReporter/create", method = RequestMethod.GET)
	public void create(@AuthenticationPrincipal(errorOnInvalidType = true) Authentication user)
	{
	}
}
