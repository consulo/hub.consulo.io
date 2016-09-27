package consulo.webService.auth.view;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

@SpringView(name = DashboardView.ID)
public class DashboardView extends VerticalLayout implements View
{
	public static final String ID = "";

	public DashboardView()
	{
		setMargin(true);
		addComponent(new Label("Dashboard"));
	}

	@Override
	public void enter(ViewChangeListener.ViewChangeEvent event)
	{
		// NOP
	}
}
