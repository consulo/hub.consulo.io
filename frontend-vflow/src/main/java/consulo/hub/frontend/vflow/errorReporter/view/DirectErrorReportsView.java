package consulo.hub.frontend.vflow.errorReporter.view;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import consulo.hub.frontend.vflow.base.MainLayout;
import consulo.hub.shared.errorReporter.domain.ErrorReport;
import consulo.hub.shared.errorReporter.domain.ErrorReportStatus;
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
		return StringUtils.containsIgnoreCase(name, "user") ||
				StringUtils.containsIgnoreCase(name, "changedByUser") ||
				StringUtils.containsIgnoreCase(name, "assignUser") ||
				StringUtils.containsIgnoreCase(name, "id") ||
				StringUtils.containsIgnoreCase(name, "changeTime");
	}

	@Override
	protected void updateHeader()
	{
		//myLabel.setValue("Error Report");
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
	protected Page<ErrorReport> getReports(int page, ErrorReportStatus[] errorReportStatuses, int pageSize)
	{
		return new PageImpl<>(Arrays.asList(myErrorReport), PageRequest.of(0, pageSize), 1);
	}
}
