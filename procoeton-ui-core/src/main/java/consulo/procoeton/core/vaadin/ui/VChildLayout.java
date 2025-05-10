package consulo.procoeton.core.vaadin.ui;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.RouteParameters;

/**
 * @author VISTALL
 * @since 30/04/2023
 */
public class VChildLayout extends VerticalLayout implements ChildLayout, BeforeEnterObserver {
    protected RouteParameters myRouteParameters = RouteParameters.empty();

    public VChildLayout() {
        setMargin(false);
        setSpacing(false);
        setSizeFull();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        myRouteParameters = event.getRouteParameters();
    }
}
