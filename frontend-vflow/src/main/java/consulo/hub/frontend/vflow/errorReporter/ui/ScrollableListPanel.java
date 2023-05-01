package consulo.hub.frontend.vflow.errorReporter.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import consulo.hub.frontend.vflow.base.util.VaadinUIUtil;

/**
 * @author VISTALL
 * @since 25-Apr-17
 */
public class ScrollableListPanel extends Scroller
{
	private final VerticalLayout myVerticalLayout = VaadinUIUtil.newVerticalLayout();

	public ScrollableListPanel()
	{
		setContent(myVerticalLayout);
		//addStyleName("v-scrollable");
		setSizeFull();
		//setDefaultComponentAlignment(Alignment.TOP_LEFT);
		//setCompositionRoot(myListLayout);
	}

	public void removeItem(Component component)
	{
		myVerticalLayout.remove(component);
	}

	public void removeAllItems()
	{
		myVerticalLayout.removeAll();
	}

	public void addItem(Component component)
	{
		myVerticalLayout.add(component);
	}

	public void addItem(Component component, FlexComponent.Alignment alignment)
	{
		addItem(component);
		//setComponentAlignment(component, alignment);
	}
}