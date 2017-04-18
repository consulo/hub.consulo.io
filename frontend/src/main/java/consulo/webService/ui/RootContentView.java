package consulo.webService.ui;

import org.jetbrains.annotations.NotNull;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;

/**
 * @author VISTALL
 * @since 18-Apr-17
 */
public class RootContentView extends HorizontalLayout
{
	private final ComponentContainer myComponentContainer;

	public RootContentView(NavigationMenu menu)
	{
		setSizeFull();
		addStyleName("mainview");

		addComponent(menu);

		myComponentContainer = new CssLayout();
		myComponentContainer.addStyleName("view-content");
		myComponentContainer.setSizeFull();
		addComponent(myComponentContainer);
		setExpandRatio(myComponentContainer, 1.0f);
	}

	@NotNull
	public ComponentContainer getComponentContainer()
	{
		return myComponentContainer;
	}
}
