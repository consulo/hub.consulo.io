package consulo.procoeton.core.vaadin.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.router.AfterNavigationEvent;
import consulo.procoeton.core.backend.BackendServiceDownException;
import consulo.procoeton.core.vaadin.util.Notifications;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 12/05/2023
 */
public abstract class ServerOfflineVChildLayout extends VChildLayout
{
	private final boolean myNeedRemoveAll;

	public ServerOfflineVChildLayout(boolean needRemoveAll)
	{
		myNeedRemoveAll = needRemoveAll;
	}

	@Override
	public final void viewReady(AfterNavigationEvent afterNavigationEvent)
	{
		if(myNeedRemoveAll)
		{
			removeAll();
		}

		try
		{
			List<Component> components = new ArrayList<>();
			buildLayout(components::add);
			add(components);
		}
		catch(BackendServiceDownException ignored)
		{
			removeAll();

			Notifications.error("Server Busy. Try Again Later");
		}
	}

	protected abstract void buildLayout(Consumer<Component> uiBuilder);
}
