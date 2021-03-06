package consulo.webService.dash.view;

import java.util.Date;
import java.util.Locale;

import javax.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import consulo.webService.errorReporter.domain.ErrorReport;
import consulo.webService.errorReporter.domain.ErrorReporterStatus;
import consulo.webService.errorReporter.mongo.ErrorReportRepository;
import consulo.webService.ui.util.TinyComponents;
import consulo.webService.ui.util.VaadinUIUtil;

@SpringView(name = DashboardView.ID)
public class DashboardView extends VerticalLayout implements View
{
	public static final String ID = "";

	@Autowired
	protected ErrorReportRepository myErrorReportRepository;

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

	private Component buildLastErrorReports(Authentication authentication)
	{
		Page<ErrorReport> reportList = myErrorReportRepository.findByReporterEmail(authentication.getName(), new PageRequest(0, 30, new Sort(Sort.Direction.DESC, ErrorReportRepository.CREATE_DATE)));

		VerticalLayout verticalLayout = VaadinUIUtil.newVerticalLayout();
		verticalLayout.addStyleName("bodyMargin");

		HorizontalLayout legendLayout = VaadinUIUtil.newHorizontalLayout();
		legendLayout.setWidth(100, Unit.PERCENTAGE);
		legendLayout.setSpacing(true);

		for(ErrorReporterStatus reporterStatus : ErrorReporterStatus.values())
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

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if(authentication == null)
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

		Component lastErrorReports = buildLastErrorReports(authentication);
		fillLayout.addComponent(lastErrorReports);
		fillLayout.setExpandRatio(lastErrorReports, 0.33f);

		addComponent(fillLayout);
		setExpandRatio(fillLayout, 1f);
	}
}
