package consulo.procoeton.core.vaadin.ui;

import com.vaadin.flow.component.html.Span;

/**
 * @author VISTALL
 * @since 30/04/2023
 */
public class Badge extends Span
{
	public Badge(String text, String... classes)
	{
		super(text);

		if(classes.length > 0)
		{
			getElement().getThemeList().add("badge " + String.join(" ", classes));
		}
		else
		{
			getElement().getThemeList().add("badge");
		}
	}
}
