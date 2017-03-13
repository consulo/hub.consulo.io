package consulo.webService.errorReporter.view;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import com.intellij.openapi.util.text.StringUtil;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import consulo.webService.errorReporter.domain.ErrorReport;
import consulo.webService.errorReporter.domain.ErrorReporterStatus;

/**
 * @author VISTALL
 * @since 11-Mar-17
 */
public class DirectErrorReportsView extends BaseErrorReportsView
{
	private ErrorReport myErrorReport;

	public DirectErrorReportsView(ErrorReport errorReport)
	{
		myErrorReport = errorReport;
	}

	@Override
	protected boolean skipField(String name)
	{
		return StringUtil.containsIgnoreCase(name, "email");
	}

	@Override
	protected void updateHeader()
	{
		myLabel.setValue("Error Report");
	}

	@Override
	protected boolean allowFilters()
	{
		return false;
	}

	@Override
	protected void addRightButtons(ErrorReport errorReport, VerticalLayout lineLayout, HorizontalLayout rightLayout, List<Consumer<ErrorReport>> onUpdate)
	{
		openOrCloseDetails(errorReport, lineLayout, onUpdate);
	}

	@Override
	protected Page<ErrorReport> getReports(int page, ErrorReporterStatus[] errorReporterStatuses, int pageSize)
	{
		return new PageImpl<>(Arrays.asList(myErrorReport), new PageRequest(0, pageSize), 1);
	}
}
