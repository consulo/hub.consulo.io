package consulo.hub.frontend.vflow.errorReporter.view;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import consulo.hub.frontend.vflow.backend.service.BackendErrorReporterService;
import consulo.hub.frontend.vflow.base.MainLayout;
import consulo.hub.frontend.vflow.base.VChildLayout;
import consulo.hub.shared.errorReporter.domain.ErrorReport;
import consulo.util.lang.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 11-Mar-17
 */
@PageTitle("Error Report")
@Route(value = "public/errorReport/:longId", layout = MainLayout.class)
@AnonymousAllowed
public class DirectErrorReportsView extends VChildLayout
{
	public static final String LONG_ID = "longId";

	@Autowired
	private final BackendErrorReporterService myErrorReportRepository;

	public DirectErrorReportsView(BackendErrorReporterService errorReportRepository)
	{
		myErrorReportRepository = errorReportRepository;
	}

	@Override
	public void viewReady(AfterNavigationEvent afterNavigationEvent)
	{
		String id = myRouteParameters.get(LONG_ID).get();

		ErrorReport report = myErrorReportRepository.findByLongId(StringUtil.notNullize(id));
		if(report == null)
		{
			report = new ErrorReport();
		}

		ErrorReportComponent component = new ErrorReportComponent(report)
		{
			@Override
			protected void addRightButtons(ErrorReport errorReport, VerticalLayout lineLayout, HorizontalLayout rightLayout, List<Consumer<ErrorReport>> onUpdate)
			{
				openOrCloseDetails(errorReport, lineLayout, onUpdate);
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
		};

		component.setWidthFull();
		add(component);
		setFlexGrow(1, component);
	}
}
