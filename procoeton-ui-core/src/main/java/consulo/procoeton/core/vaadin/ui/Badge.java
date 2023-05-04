package consulo.procoeton.core.vaadin.ui;

import com.vaadin.flow.component.html.Span;

/**
 * @author VISTALL
 * @since 30/04/2023
 */
public class Badge extends Span
{
	public Badge(String text)
	{
		super(text);

		getElement().getThemeList().add("badge");
	}
}
