package consulo.procoeton.core.vaadin.ui;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import java.util.function.Supplier;

/**
 * @author VISTALL
 * @since 05/05/2023
 */
public class LazyComponent extends HorizontalLayout
{
	private final Supplier<Component> myComponentSupplier;

	private boolean myInitialized;

	public LazyComponent(Supplier<Component> componentSupplier)
	{
		myComponentSupplier = componentSupplier;
	}

	@Override
	protected void onAttach(AttachEvent attachEvent)
	{
		super.onAttach(attachEvent);

		if(!myInitialized)
		{
			Component component = myComponentSupplier.get();

			add(component);

			myInitialized = true;
		}
	}
}
