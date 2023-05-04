package consulo.procoeton.core.vaadin.util;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.router.RouteParameters;
import consulo.procoeton.core.vaadin.ui.ChildLayout;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author VISTALL
 * @since 30/04/2023
 */
public class RouterUtil
{
	public static <C extends Component & ChildLayout> void updateUrl(Class<? extends C> childLayout, Supplier<RouteParameters> parametersSupplier, Map<String, String> override)
	{
		Map<String, String> map = new HashMap<>();
		RouteParameters originalParameters = parametersSupplier.get();
		for(String parameterName : originalParameters.getParameterNames())
		{
			originalParameters.get(parameterName).ifPresent(value -> map.put(parameterName, value));
		}

		map.putAll(override);

		RouteParameters parameters = new RouteParameters(map);

		String url = RouteConfiguration.forSessionScope().getUrl(childLayout, parameters);

		UI.getCurrent().getPage().getHistory().pushState(null, url);
	}
}
