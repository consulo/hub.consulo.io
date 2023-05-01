package consulo.hub.frontend.vflow.dash.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import consulo.hub.frontend.vflow.backend.service.BackendErrorReporterService;
import consulo.hub.frontend.vflow.base.LabeledLayout;
import consulo.hub.frontend.vflow.base.MainLayout;
import consulo.hub.frontend.vflow.base.VChildLayout;
import consulo.hub.frontend.vflow.base.util.TinyComponents;
import consulo.hub.frontend.vflow.base.util.VaadinUIUtil;
import consulo.hub.shared.auth.SecurityUtil;
import consulo.hub.shared.auth.domain.UserAccount;
import consulo.hub.shared.errorReporter.domain.ErrorReport;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import javax.annotation.Nonnull;

@PageTitle("Dashboard")
@Route(value = "dashboard", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@PermitAll
public class DashboardView extends VChildLayout
{
	@Autowired
	protected BackendErrorReporterService myErrorReportRepository;

	public DashboardView()
	{
	}

	private Component buildLastPluginComments()
	{
		VerticalLayout verticalLayout = VaadinUIUtil.newVerticalLayout();
		verticalLayout.setSizeFull();
		//verticalLayout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		verticalLayout.add(TinyComponents.newLabel("Not Implemented Yet"));

		return panel("Last Plugin Comments", verticalLayout);
	}

	private Component buildLastSettingsUpdate()
	{
		VerticalLayout verticalLayout = VaadinUIUtil.newVerticalLayout();
		verticalLayout.setSizeFull();
		//verticalLayout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		verticalLayout.add(TinyComponents.newLabel("Not Implemented Yet"));

		return panel("Last Settings Update", verticalLayout);
	}

	private Component buildLastErrorReports(UserAccount userAccount)
	{
		Page<ErrorReport> reportList = myErrorReportRepository.findByUser(userAccount.getId(), PageRequest.of(0, 30, Sort.by(Sort.Direction.DESC, BackendErrorReporterService.CREATE_DATE)));

		VerticalLayout verticalLayout = VaadinUIUtil.newVerticalLayout();
		verticalLayout.addClassName("bodyMargin");

		//		HorizontalLayout legendLayout = VaadinUIUtil.newHorizontalLayout();
		//		legendLayout.setWidth(100, Unit.PERCENTAGE);
		//		legendLayout.setSpacing(true);
		//
		//		for(ErrorReportStatus reporterStatus : ErrorReportStatus.values())
		//		{
		//			VerticalLayout lineLayout = VaadinUIUtil.newVerticalLayout();
		//
		//			Label label = TinyComponents.newLabel(StringUtil.capitalize(reporterStatus.name().toLowerCase(Locale.US)));
		//			label.addStyleName(ValoTheme.LABEL_BOLD);
		//			lineLayout.addComponent(label);
		//			lineLayout.addStyleName("errorViewLineLayout");
		//			lineLayout.addStyleName("errorViewLineLayout" + StringUtil.capitalize(reporterStatus.name().toLowerCase(Locale.US)));
		//			legendLayout.addComponent(lineLayout);
		//		}
		//		verticalLayout.addComponent(legendLayout);
		//
		//		for(ErrorReport errorReport : reportList)
		//		{
		//			HorizontalLayout shortLine = VaadinUIUtil.newHorizontalLayout();
		//			shortLine.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
		//			shortLine.setWidth(100, Unit.PERCENTAGE);
		//
		//			VerticalLayout lineLayout = VaadinUIUtil.newVerticalLayout();
		//			lineLayout.setWidth(100, Unit.PERCENTAGE);
		//			lineLayout.addComponent(shortLine);
		//			lineLayout.addStyleName("errorViewLineLayout");
		//
		//			lineLayout.addStyleName("errorViewLineLayout" + StringUtil.capitalize(errorReport.getStatus().name().toLowerCase(Locale.US)));
		//
		//			HorizontalLayout leftLayout = VaadinUIUtil.newHorizontalLayout();
		//			leftLayout.setWidth(100, Unit.PERCENTAGE);
		//			leftLayout.setSpacing(true);
		//			leftLayout.addComponent(TinyComponents.newLabel("Message: " + StringUtil.shortenTextWithEllipsis(errorReport.getMessage(), 30, 10)));
		//			leftLayout.addComponent(TinyComponents.newLabel("At: " + new Date(errorReport.getCreateDate())));
		//
		//			shortLine.addComponent(leftLayout);
		//			shortLine.setComponentAlignment(leftLayout, Alignment.MIDDLE_LEFT);
		//
		//			verticalLayout.addComponent(lineLayout);
		//		}

		return panel("Last Error Reports", verticalLayout);
	}

	@Nonnull
	private static Component panel(String cap, Component component)
	{
		LabeledLayout panel = new LabeledLayout(cap, component);
		panel.setSizeFull();
		return panel;
	}

	@Override
	public void viewReady(AfterNavigationEvent event)
	{
		removeAll();

		UserAccount userAccount = SecurityUtil.getUserAccout();
		if(userAccount == null)
		{
			return;
		}

		HorizontalLayout fillLayout = VaadinUIUtil.newHorizontalLayout();
		fillLayout.addClassName("bodyMargin");
		fillLayout.setSizeFull();
		fillLayout.setSpacing(true);
		//fillLayout.setDefaultComponentAlignment(Alignment.TOP_CENTER);

		Component lastPluginComments = buildLastPluginComments();
		fillLayout.add(lastPluginComments);
		//fillLayout.setExpandRatio(lastPluginComments, 0.33f);

		Component lastSettingsUpdate = buildLastSettingsUpdate();
		fillLayout.add(lastSettingsUpdate);
		//fillLayout.setExpandRatio(lastSettingsUpdate, 0.33f);

		Component lastErrorReports = buildLastErrorReports(userAccount);
		fillLayout.add(lastErrorReports);
		//fillLayout.setExpandRatio(lastErrorReports, 0.33f);

		add(fillLayout);
		setFlexGrow(1f, fillLayout);
	}
}
