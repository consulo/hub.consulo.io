package consulo.webService.ui.util;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;

/**
 * @author VISTALL
 * @since 09-Nov-16
 */
public class VaadinUIUtil
{
	public static Component labeled(String name, Component component)
	{
		HorizontalLayout horizontalLayout = new HorizontalLayout(TidyComponents.newLabel(name), component);
		horizontalLayout.setSpacing(true);
		horizontalLayout.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
		return horizontalLayout;
	}
}
