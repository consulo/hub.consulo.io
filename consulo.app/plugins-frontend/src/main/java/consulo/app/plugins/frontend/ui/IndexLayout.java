package consulo.app.plugins.frontend.ui;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import consulo.procoeton.core.vaadin.ui.VChildLayout;

/**
 * @author VISTALL
 * @since 2025-05-10
 */
@Route(value = "/", layout = PluginsAppLayout.class)
@AnonymousAllowed
public class IndexLayout extends VChildLayout {
    public IndexLayout() {
        add(new Span("Test"));
    }
}
