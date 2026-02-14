package consulo.procoeton.core.vaadin;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.page.ColorScheme;
import com.vaadin.flow.router.*;
import consulo.procoeton.core.vaadin.ui.ChildLayout;

/**
 * @author VISTALL
 * @since 2025-05-11
 */
@PreserveOnRefresh
public class SimpleAppLayout extends AppLayout implements AfterNavigationObserver, BeforeEnterObserver {
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

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        handleDarkTheme();
    }

    protected void handleDarkTheme() {
        getUI().ifPresent(ui -> ui.getPage().executeJs("return window.matchMedia('(prefers-color-scheme: dark)').matches;").then(Boolean.class, isDark -> {
            ui.getPage().setColorScheme(ColorScheme.Value.DARK);

            ThemeUtil.notifyUpdate();
        }));
    }
}
