package consulo.cloud.www.frontend.ui.view;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import consulo.procoeton.core.vaadin.SimpleAppLayout;
import consulo.procoeton.core.vaadin.ui.VChildLayout;

/**
 * @author VISTALL
 * @since 2025-05-15
 */
@Route(value = "/", layout = SimpleAppLayout.class)
@AnonymousAllowed
public class IndexView extends VChildLayout {
    public IndexView() {
        H1 h1 = new H1("Work in Progress...");
        h1.getStyle().setAlignSelf(Style.AlignSelf.CENTER);
        h1.getStyle().set("alignContent", "center");
        h1.setHeightFull();
        add(h1);
    }
}
