package consulo.procoeton.core.vaadin.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.router.AfterNavigationEvent;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 30/04/2023
 */
public interface ChildLayout extends HasComponents
{
	default void viewReady(AfterNavigationEvent afterNavigationEvent)
	{
	}

	@Nullable
	default Component getHeaderRightComponent()
	{
		return null;
	}
}
