package consulo.webService.auth.view;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import consulo.webService.ui.util.TidyComponents;

@SpringView(name = DashboardView.ID)
public class DashboardView extends VerticalLayout implements View
{
	public static final String ID = "";

	public DashboardView()
	{
		setMargin(true);
		setSizeFull();
		addComponent(new Label("Dashboard"));

		VerticalSplitPanel panel = new VerticalSplitPanel();
		panel.setSizeFull();
		panel.setSplitPosition(50, Unit.PERCENTAGE);

		addComponent(panel);
		setExpandRatio(panel, 0.9f);

		HorizontalLayout topLayout = new HorizontalLayout();
		topLayout.setSizeFull();
		panel.setFirstComponent(topLayout);

		HorizontalLayout bottomLayout = new HorizontalLayout();
		bottomLayout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		bottomLayout.setSizeFull();

		bottomLayout.addComponent(buildLastPluginComments());
		bottomLayout.addComponent(buildLastSettingsUpdate());
		bottomLayout.addComponent(buildLastErrorReports());

		panel.setSecondComponent(bottomLayout);
	}

	private Component buildLastPluginComments()
	{
		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.setHeight(100, Unit.PERCENTAGE);
		verticalLayout.setWidth(20, Unit.EM);

		verticalLayout.addComponent(new Label("Last Plugin Comments:"));
		verticalLayout.addComponent(TidyComponents.newLabel("TODO"));
		return verticalLayout;
	}

	private Component buildLastSettingsUpdate()
	{
		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.setHeight(100, Unit.PERCENTAGE);
		verticalLayout.setWidth(20, Unit.EM);

		verticalLayout.addComponent(new Label("Last Settigs Update:"));
		verticalLayout.addComponent(TidyComponents.newLabel("TODO"));
		return verticalLayout;
	}

	private Component buildLastErrorReports()
	{
		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.setHeight(100, Unit.PERCENTAGE);
		verticalLayout.setWidth(20, Unit.EM);

		verticalLayout.addComponent(new Label("Last Error Reports:"));
		verticalLayout.addComponent(TidyComponents.newLabel("TODO"));
		return verticalLayout;
	}

	@Override
	public void enter(ViewChangeListener.ViewChangeEvent event)
	{
		// NOP
	}
}
