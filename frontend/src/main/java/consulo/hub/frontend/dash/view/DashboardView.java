package consulo.hub.frontend.dash.view;

import com.intellij.openapi.util.text.StringUtil;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import consulo.hub.frontend.backend.service.ErrorReporterService;
import consulo.hub.frontend.base.ui.util.TinyComponents;
import consulo.hub.frontend.base.ui.util.VaadinUIUtil;
import consulo.hub.shared.auth.SecurityUtil;
import consulo.hub.shared.auth.domain.UserAccount;
import consulo.hub.shared.errorReporter.domain.ErrorReport;
import consulo.hub.shared.errorReporter.domain.ErrorReportStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import javax.annotation.Nonnull;
import java.util.Date;
import java.util.Locale;

@SpringView(name = DashboardView.ID)
public class DashboardView extends VerticalLayout implements View
{
	public static final String ID = "";

	@Autowired
	protected ErrorReporterService myErrorReportRepository;

	public DashboardView()
	{
		setMargin(false);
		setSpacing(false);
		setSizeFull();
	}

	private Component buildLastPluginComments()
	{
		VerticalLayout verticalLayout = VaadinUIUtil.newVerticalLayout();
		verticalLayout.setSizeFull();
		verticalLayout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		verticalLayout.addComponent(TinyComponents.newLabel("Not Implemented Yet"));

		return panel("Last Plugin Comments", verticalLayout);
	}

	private Component buildLastSettingsUpdate()
	{
		VerticalLayout verticalLayout = VaadinUIUtil.newVerticalLayout();
		verticalLayout.setSizeFull();
		verticalLayout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		verticalLayout.addComponent(TinyComponents.newLabel("Not Implemented Yet"));

		return panel("Last Settings Update", verticalLayout);
	}

	private Component buildLastErrorReports(UserAccount userAccount)
	{
		Page<ErrorReport> reportList = myErrorReportRepository.findByReporterEmail(userAccount.getId(), new PageRequest(0, 30, new Sort(Sort.Direction.DESC, ErrorReporterService.CREATE_DATE)));

		VerticalLayout verticalLayout = VaadinUIUtil.newVerticalLayout();
		verticalLayout.addStyleName("bodyMargin");

		HorizontalLayout legendLayout = VaadinUIUtil.newHorizontalLayout();
		legendLayout.setWidth(100, Unit.PERCENTAGE);
		legendLayout.setSpacing(true);

		for(ErrorReportStatus reporterStatus : ErrorReportStatus.values())
		{
			VerticalLayout lineLayout = VaadinUIUtil.newVerticalLayout();

			Label label = TinyComponents.newLabel(StringUtil.capitalize(reporterStatus.name().toLowerCase(Locale.US)));
			label.addStyleName(ValoTheme.LABEL_BOLD);
			lineLayout.addComponent(label);
			lineLayout.addStyleName("errorViewLineLayout");
			lineLayout.addStyleName("errorViewLineLayout" + StringUtil.capitalize(reporterStatus.name().toLowerCase(Locale.US)));
			legendLayout.addComponent(lineLayout);
		}
		verticalLayout.addComponent(legendLayout);

		for(ErrorReport errorReport : reportList)
		{
			HorizontalLayout shortLine = VaadinUIUtil.newHorizontalLayout();
			shortLine.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
			shortLine.setWidth(100, Unit.PERCENTAGE);

			VerticalLayout lineLayout = VaadinUIUtil.newVerticalLayout();
			lineLayout.setWidth(100, Unit.PERCENTAGE);
			lineLayout.addComponent(shortLine);
			lineLayout.addStyleName("errorViewLineLayout");

			lineLayout.addStyleName("errorViewLineLayout" + StringUtil.capitalize(errorReport.getStatus().name().toLowerCase(Locale.US)));

			HorizontalLayout leftLayout = VaadinUIUtil.newHorizontalLayout();
			leftLayout.setWidth(100, Unit.PERCENTAGE);
			leftLayout.setSpacing(true);
			leftLayout.addComponent(TinyComponents.newLabel("Message: " + StringUtil.shortenTextWithEllipsis(errorReport.getMessage(), 30, 10)));
			leftLayout.addComponent(TinyComponents.newLabel("At: " + new Date(errorReport.getCreateDate())));

			shortLine.addComponent(leftLayout);
			shortLine.setComponentAlignment(leftLayout, Alignment.MIDDLE_LEFT);

			verticalLayout.addComponent(lineLayout);
		}

		return panel("Last Error Reports", verticalLayout);
	}

	@Nonnull
	private static Panel panel(String cap, Component component)
	{
		Panel panel = new Panel(cap, component);
		panel.setSizeFull();
		return panel;
	}

	@Override
	public void enter(ViewChangeListener.ViewChangeEvent event)
	{
		removeAllComponents();

		Label label = new Label("Dashboard");
		label.addStyleName("headerMargin");
		addComponent(label);

		UserAccount userAccount = SecurityUtil.getUserAccout();
		if(userAccount == null)
		{
			return;
		}

		HorizontalLayout fillLayout = VaadinUIUtil.newHorizontalLayout();
		fillLayout.addStyleName("bodyMargin");
		fillLayout.setSizeFull();
		fillLayout.setSpacing(true);
		fillLayout.setDefaultComponentAlignment(Alignment.TOP_CENTER);

		Component lastPluginComments = buildLastPluginComments();
		fillLayout.addComponent(lastPluginComments);
		fillLayout.setExpandRatio(lastPluginComments, 0.33f);

		Component lastSettingsUpdate = buildLastSettingsUpdate();
		fillLayout.addComponent(lastSettingsUpdate);
		fillLayout.setExpandRatio(lastSettingsUpdate, 0.33f);

		Component lastErrorReports = buildLastErrorReports(userAccount);
		fillLayout.addComponent(lastErrorReports);
		fillLayout.setExpandRatio(lastErrorReports, 0.33f);

		addComponent(fillLayout);
		setExpandRatio(fillLayout, 1f);
	}
}
