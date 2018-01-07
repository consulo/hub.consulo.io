package consulo.webService.errorReporter.view;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import consulo.webService.ui.util.TinyComponents;
import consulo.webService.ui.util.VaadinUIUtil;

/**
 * @author VISTALL
 * @since 06-Jun-17
 */
@SpringView(name = ErrorStatisticsView.ID)
public class ErrorStatisticsView extends VerticalLayout implements View
{
	public static final String ID = "errorReportStatistics";

	public ErrorStatisticsView()
	{
		setMargin(false);
		setSpacing(false);
		setSizeFull();
	}

	@Override
	public void enter(ViewChangeListener.ViewChangeEvent event)
	{
		removeAllComponents();

		HorizontalLayout header = VaadinUIUtil.newHorizontalLayout();
		header.addStyleName("headerMargin");
		header.setWidth(100, Unit.PERCENTAGE);
		Label myLabel = new Label("Error Report Statistics");

		header.addComponent(myLabel);

		HorizontalLayout layout = VaadinUIUtil.newHorizontalLayout();
		layout.addComponent(TinyComponents.newLabel("Not Implemented Yet"));
		layout.addStyleName("bodyMargin");
		addComponent(layout);

		setExpandRatio(layout, 1);
	}
}
