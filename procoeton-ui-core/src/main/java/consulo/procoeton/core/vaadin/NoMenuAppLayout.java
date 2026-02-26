package consulo.procoeton.core.vaadin;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.ColorScheme;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinRequest;
import consulo.procoeton.core.vaadin.ui.ChildLayout;
import jakarta.servlet.http.Cookie;

/**
 * @author VISTALL
 * @since 2026-02-25
 */
public class NoMenuAppLayout extends VerticalLayout implements RouterLayout, AfterNavigationObserver, BeforeEnterObserver {
    public NoMenuAppLayout() {
        setPadding(false);
        setMargin(false);
        setSpacing(false);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
    }

    protected void handleHeaderRightComponent(Component headerRightComponent) {
    }

    @Override
    public void afterNavigation(AfterNavigationEvent afterNavigationEvent) {
        for (Component content : getChildren().toList()) {
            if (content instanceof ChildLayout childLayout) {
                Component headerRightComponent = childLayout.getHeaderRightComponent();
                if (headerRightComponent != null) {
                    handleHeaderRightComponent(headerRightComponent);
                }

                childLayout.viewReady(afterNavigationEvent);
            }
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        handleDarkTheme();
    }

    protected void handleDarkTheme() {
        boolean darkTheme = readDarkThemeCookie();

        getUI().ifPresent(ui -> {
            if (darkTheme) {
                ui.getPage().setColorScheme(ColorScheme.Value.DARK);
            }
            ui.getPage().executeJs("document.documentElement.classList.remove('dark-loading')");
        });

        ThemeUtil.notifyUpdate();
    }

    protected boolean readDarkThemeCookie() {
        VaadinRequest request = VaadinRequest.getCurrent();
        if (request != null) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("darkTheme".equals(cookie.getName())) {
                        return Boolean.parseBoolean(cookie.getValue());
                    }
                }
            }
        }
        return false;
    }
}
