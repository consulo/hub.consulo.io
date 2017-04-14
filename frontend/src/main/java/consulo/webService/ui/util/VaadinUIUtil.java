package consulo.webService.ui.util;

import com.vaadin.server.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;

/**
 * @author VISTALL
 * @since 09-Nov-16
 */
public class VaadinUIUtil
{
	public static Component labeledFill(String name, Component component)
	{
		GridLayout gridLayout = new GridLayout(2, 1);
		gridLayout.setWidth(100, Sizeable.Unit.PERCENTAGE);
		component.setWidth(100, Sizeable.Unit.PERCENTAGE);
		gridLayout.addComponent(TidyComponents.newLabel(name), 0, 0);
		gridLayout.addComponent(component, 1, 0);
		return gridLayout;
	}

	public static Component labeled(String name, Component component)
	{
		HorizontalLayout horizontalLayout = new HorizontalLayout();
		horizontalLayout.setSpacing(true);
		horizontalLayout.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
		horizontalLayout.addComponent(TidyComponents.newLabel(name));
		horizontalLayout.addComponent(component);
		return horizontalLayout;
	}
}
