package consulo.webService.ui.util;

import com.vaadin.server.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;

/**
 * @author VISTALL
 * @since 09-Nov-16
 */
public class VaadinUIUtil
{
	public static VerticalLayout newVerticalLayout()
	{
		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.setMargin(false);
		verticalLayout.setSpacing(false);
		return verticalLayout;
	}

	public static HorizontalLayout newHorizontalLayout()
	{
		HorizontalLayout horizontalLayout = new HorizontalLayout();
		horizontalLayout.setMargin(false);
		horizontalLayout.setSpacing(false);
		return horizontalLayout;
	}

	public static Component labeledFill(String name, Component component)
	{
		GridLayout gridLayout = new GridLayout(2, 1);
		gridLayout.setMargin(false);
		gridLayout.setSpacing(false);
		gridLayout.setWidth(100, Sizeable.Unit.PERCENTAGE);
		component.setWidth(100, Sizeable.Unit.PERCENTAGE);
		gridLayout.addComponent(TinyComponents.newLabel(name), 0, 0);
		gridLayout.addComponent(component, 1, 0);
		return gridLayout;
	}

	public static Component labeled(String name, Component component)
	{
		HorizontalLayout horizontalLayout = VaadinUIUtil.newHorizontalLayout();
		horizontalLayout.setSpacing(true);
		horizontalLayout.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
		horizontalLayout.addComponent(TinyComponents.newLabel(name));
		horizontalLayout.addComponent(component);
		return horizontalLayout;
	}
}
