package consulo.hub.frontend.errorReporter.view;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import consulo.hub.shared.errorReporter.domain.ErrorReport;
import consulo.hub.shared.errorReporter.domain.ErrorReporterStatus;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

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
		return StringUtils.containsIgnoreCase(name, "email");
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
