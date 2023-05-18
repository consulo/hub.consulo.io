package consulo.hub.frontend.vflow.dash.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.theme.lumo.LumoUtility;
import consulo.hub.frontend.vflow.backend.service.BackendErrorReporterService;
import consulo.hub.frontend.vflow.base.MainLayout;
import consulo.hub.shared.auth.SecurityUtil;
import consulo.hub.shared.auth.domain.UserAccount;
import consulo.hub.shared.errorReporter.domain.ErrorReport;
import consulo.hub.shared.errorReporter.domain.ErrorReportStatus;
import consulo.procoeton.core.vaadin.ui.LabeledLayout;
import consulo.procoeton.core.vaadin.ui.ServerOfflineVChildLayout;
import consulo.procoeton.core.vaadin.ui.util.VaadinUIUtil;
import consulo.util.lang.StringUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Date;
import java.util.Locale;
import java.util.function.Consumer;

@PageTitle("Dashboard")
@Route(value = "user/dashboard", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@PermitAll
public class DashboardView extends ServerOfflineVChildLayout
{
	@Autowired
	protected BackendErrorReporterService myErrorReportRepository;

	public DashboardView()
	{
		super(true);
	}

	private Component buildLastPluginComments()
	{
		VerticalLayout verticalLayout = VaadinUIUtil.newVerticalLayout();
		verticalLayout.setSizeFull();
		//verticalLayout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		verticalLayout.add(new Label("Not Implemented Yet"));

		return panel("Last Plugin Comments", verticalLayout);
	}

	private Component buildLastSettingsUpdate()
	{
		VerticalLayout verticalLayout = VaadinUIUtil.newVerticalLayout();
		verticalLayout.setSizeFull();
		//verticalLayout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		verticalLayout.add(new Label("Not Implemented Yet"));

		return panel("Last Settings Update", verticalLayout);
	}

	private Component buildLastErrorReports()
	{
		Page<ErrorReport> reportList = myErrorReportRepository.findByUser(-1, PageRequest.of(0, 30, Sort.by(Sort.Direction.DESC, BackendErrorReporterService.CREATE_DATE)));

		VerticalLayout verticalLayout = VaadinUIUtil.newVerticalLayout();
		verticalLayout.addClassName("bodyMargin");

		HorizontalLayout legendLayout = VaadinUIUtil.newHorizontalLayout();
		legendLayout.setWidth(100, Unit.PERCENTAGE);
		legendLayout.setSpacing(true);

		for(ErrorReportStatus reporterStatus : ErrorReportStatus.values())
		{
			VerticalLayout lineLayout = VaadinUIUtil.newVerticalLayout();

			Label label = new Label(StringUtil.capitalize(reporterStatus.name().toLowerCase(Locale.US)));
			label.addClassName(LumoUtility.FontWeight.BOLD);
			lineLayout.add(label);
			//lineLayout.addStyleName("errorViewLineLayout");
			//lineLayout.addStyleName("errorViewLineLayout" + StringUtil.capitalize(reporterStatus.name().toLowerCase(Locale.US)));
			legendLayout.add(lineLayout);
		}
		verticalLayout.add(legendLayout);

		for(ErrorReport errorReport : reportList)
		{
			HorizontalLayout shortLine = VaadinUIUtil.newHorizontalLayout();
			//shortLine.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
			shortLine.setWidth(100, Unit.PERCENTAGE);

			VerticalLayout lineLayout = VaadinUIUtil.newVerticalLayout();
			lineLayout.setWidth(100, Unit.PERCENTAGE);
			lineLayout.add(shortLine);
			//lineLayout.addStyleName("errorViewLineLayout");

			//lineLayout.addStyleName("errorViewLineLayout" + StringUtil.capitalize(errorReport.getStatus().name().toLowerCase(Locale.US)));

			HorizontalLayout leftLayout = VaadinUIUtil.newHorizontalLayout();
			leftLayout.setWidth(100, Unit.PERCENTAGE);
			leftLayout.setSpacing(true);
			leftLayout.add(new Label("Message: " + StringUtil.shortenTextWithEllipsis(errorReport.getMessage(), 30, 10)));
			leftLayout.add(new Label("At: " + new Date(errorReport.getCreateDate())));

			shortLine.add(leftLayout);
			//shortLine.setComponentAlignment(leftLayout, Alignment.MIDDLE_LEFT);

			verticalLayout.add(lineLayout);
		}

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
	protected void buildLayout(Consumer<Component> uiBuilder)
	{
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

		Component lastErrorReports = buildLastErrorReports();
		fillLayout.add(lastErrorReports);
		//fillLayout.setExpandRatio(lastErrorReports, 0.33f);

		uiBuilder.accept(fillLayout);
	}
}
