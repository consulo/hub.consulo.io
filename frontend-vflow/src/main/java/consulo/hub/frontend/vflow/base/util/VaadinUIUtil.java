package consulo.hub.frontend.vflow.base.util;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

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

	public static HorizontalLayout newHorizontalLayout(Component component)
	{
		HorizontalLayout layout = newHorizontalLayout();
		layout.add(component);
		return layout;
	}

	public static HorizontalLayout newHorizontalLayout()
	{
		HorizontalLayout horizontalLayout = new HorizontalLayout();
		horizontalLayout.setMargin(false);
		horizontalLayout.setSpacing(false);
		return horizontalLayout;
	}

//	public static Component labeledFill(String name, Component component)
//	{
//		GridLayout gridLayout = new GridLayout(2, 1);
//		gridLayout.setMargin(false);
//		gridLayout.setSpacing(false);
//		gridLayout.setWidth(100, Sizeable.Unit.PERCENTAGE);
//		component.setWidth(100, Sizeable.Unit.PERCENTAGE);
//		gridLayout.addComponent(TinyComponents.newLabel(name), 0, 0);
//		gridLayout.addComponent(component, 1, 0);
//		return gridLayout;
//	}
//
	public static Component labeled(String name, Component component)
	{
		HorizontalLayout horizontalLayout = VaadinUIUtil.newHorizontalLayout();
		horizontalLayout.setSpacing(true);
		horizontalLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
		horizontalLayout.add(TinyComponents.newLabel(name));
		horizontalLayout.add(component);
		return horizontalLayout;
	}
}
