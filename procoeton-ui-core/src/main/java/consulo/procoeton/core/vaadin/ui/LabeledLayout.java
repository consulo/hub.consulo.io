package consulo.procoeton.core.vaadin.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.ThemableLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;

/**
 * @author VISTALL
 * @since 30/04/2023
 */
public class LabeledLayout extends VerticalLayout
{
	public LabeledLayout(String caption, Component component)
	{
		setMargin(false);
		Span span = new Span(caption);
		span.addClassName(LumoUtility.FontWeight.BOLD);
		span.setWidthFull();
		add(span);
		add(component);

		if(component instanceof ThemableLayout themableLayout)
		{
			themableLayout.setMargin(false);
			themableLayout.setPadding(false);
		}

		addClassName(LumoUtility.Border.ALL);
		addClassName(LumoUtility.BorderRadius.SMALL);
		addClassName(LumoUtility.BorderColor.CONTRAST_10);
	}
}
