package consulo.app.plugins.frontend.ui;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import consulo.procoeton.core.vaadin.ui.VChildLayout;

import java.util.Optional;

/**
 * @author VISTALL
 * @since 2025-05-11
 */
@Route(value = "/v/:pluginId/:pluginName?", layout = PluginsAppLayout.class)
@AnonymousAllowed
public class PluginView extends VChildLayout {
    public static final String PLUGIN_ID = "pluginId";
    public static final String PLUGIN_NAME = "pluginName";

    public PluginView() {
        add("test");
    }

    @Override
    public void viewReady(AfterNavigationEvent afterNavigationEvent) {
        RouteParameters parameters = afterNavigationEvent.getRouteParameters();

        Optional<String> pluginId = parameters.get(PLUGIN_ID);

        add(new Span(pluginId.get()));
    }
}
