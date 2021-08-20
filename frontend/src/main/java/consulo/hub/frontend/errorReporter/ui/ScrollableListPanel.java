package consulo.hub.frontend.errorReporter.ui;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.VerticalLayout;
import consulo.hub.frontend.base.ui.util.VaadinUIUtil;

/**
 * @author VISTALL
 * @since 25-Apr-17
 */
public class ScrollableListPanel extends CustomComponent
{
	private VerticalLayout myListLayout = VaadinUIUtil.newVerticalLayout();

	public ScrollableListPanel()
	{
		addStyleName("v-scrollable");
		setSizeFull();
		myListLayout.setDefaultComponentAlignment(Alignment.TOP_LEFT);
		setCompositionRoot(myListLayout);
	}

	public void add(Component component)
	{
		myListLayout.addComponent(component);
	}

	public void add(Component component, Alignment alignment)
	{
		myListLayout.addComponent(component);
		myListLayout.setComponentAlignment(component, alignment);
	}

	public void remove(Component component)
	{
		myListLayout.removeComponent(component);
	}

	public void removeAll()
	{
		myListLayout.removeAllComponents();
	}
}