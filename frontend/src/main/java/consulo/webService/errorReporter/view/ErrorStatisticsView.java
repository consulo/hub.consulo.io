package consulo.webService.errorReporter.view;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.VerticalLayout;

/**
 * @author VISTALL
 * @since 06-Jun-17
 */
@SpringView(name = ErrorStatisticsView.ID)
public class ErrorStatisticsView extends VerticalLayout implements View
{
	public static final String ID = "errorStatistics";

	@Override
	public void enter(ViewChangeListener.ViewChangeEvent event)
	{

	}
}
