package consulo.procoeton.core.vaadin.service;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.RouteAliasData;
import com.vaadin.flow.router.RouteData;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.server.RouteRegistry;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author VISTALL
 * @since 04/05/2023
 */
@Component
public class ProVaadinServiceInitListener implements VaadinServiceInitListener
{
	private final ProMainLayoutProvider myProMainLayoutProvider;

	@Autowired
	public ProVaadinServiceInitListener(ProMainLayoutProvider proMainLayoutProvider)
	{
		myProMainLayoutProvider = proMainLayoutProvider;
	}

	@Override
	public void serviceInit(ServiceInitEvent se)
	{
		VaadinService source = se.getSource();

		source.addUIInitListener(event -> {
			UI ui = event.getUI();

			// add to service?
		});

		// Since we don't ref primary layout in annotations @Route since its expose all IMPl to API module
		// we re-register routers with correct layout
		RouteRegistry registry = source.getRouter().getRegistry();

		List<RouteData> registeredRoutes = registry.getRegisteredRoutes();

		registry.clean();

		List<Class<? extends RouterLayout>> parentLayouts = List.of(myProMainLayoutProvider.getLayoutClass());

		for(RouteData route : registeredRoutes)
		{
			registry.setRoute(route.getTemplate(), route.getNavigationTarget(), parentLayouts);

			// register alias too
			for(RouteAliasData data : route.getRouteAliases())
			{
				registry.setRoute(data.getTemplate(), data.getNavigationTarget(), parentLayouts);
			}
		}
	}
}
