package consulo.procoeton.core.vaadin.ui.util;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.html.Label;
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

	public static Component labeledFill(String name, Component component)
	{
		HorizontalLayout layout = newHorizontalLayout();
		layout.setWidthFull();
		layout.add(new Label(name));
		layout.add(component);
		layout.setFlexGrow(1, component);
		if(component instanceof HasSize)
		{
			((HasSize) component).setWidthFull();
		}
		return layout;
	}

	public static Component labeled(String name, Component component)
	{
		HorizontalLayout horizontalLayout = VaadinUIUtil.newHorizontalLayout();
		horizontalLayout.setSpacing(true);
		horizontalLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
		horizontalLayout.add(new Label(name));
		horizontalLayout.add(component);
		return horizontalLayout;
	}
}
