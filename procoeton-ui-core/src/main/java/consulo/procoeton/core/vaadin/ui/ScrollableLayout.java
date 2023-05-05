package consulo.procoeton.core.vaadin.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import consulo.procoeton.core.vaadin.ui.util.VaadinUIUtil;

/**
 * @author VISTALL
 * @since 25-Apr-17
 */
public class ScrollableLayout extends Scroller
{
	private final VerticalLayout myVerticalLayout = VaadinUIUtil.newVerticalLayout();

	public ScrollableLayout()
	{
		setContent(myVerticalLayout);
		setSizeFull();
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
}