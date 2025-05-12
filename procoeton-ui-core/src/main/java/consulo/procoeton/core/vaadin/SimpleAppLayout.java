package consulo.procoeton.core.vaadin;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.Lumo;
import consulo.procoeton.core.vaadin.ui.ChildLayout;

/**
 * @author VISTALL
 * @since 2025-05-11
 */
@PreserveOnRefresh
public class SimpleAppLayout extends AppLayout implements AfterNavigationObserver, BeforeEnterObserver {
    @Override
    protected void afterNavigation() {
        super.afterNavigation();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
    }

    protected void handleHeaderRightComponent(Component headerRightComponent) {
    }

    @Override
    public void afterNavigation(AfterNavigationEvent afterNavigationEvent) {
        Component content = getContent();
        if (content instanceof ChildLayout childLayout) {
            Component headerRightComponent = childLayout.getHeaderRightComponent();
            if (headerRightComponent != null) {
                handleHeaderRightComponent(headerRightComponent);
            }

            childLayout.viewReady(afterNavigationEvent);
        }
    }
}
