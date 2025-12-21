package consulo.app.plugins.frontend.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.page.ColorScheme;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.LumoUtility;
import consulo.procoeton.core.vaadin.SimpleAppLayout;
import consulo.procoeton.core.vaadin.ThemeChangeNotifier;
import consulo.procoeton.core.vaadin.ThemeUtil;
import jakarta.annotation.security.PermitAll;
import org.vaadin.lineawesome.LineAwesomeIcon;

/**
 * @author VISTALL
 * @since 2025-05-11
 */
@PermitAll
@PreserveOnRefresh
public class PluginsAppLayout extends SimpleAppLayout implements ThemeChangeNotifier {
    private Div myThemeIconHolder;

    public PluginsAppLayout() {
        H1 title = new H1("plugins.consulo.app");
        title.getStyle().set("font-size", "var(--lumo-font-size-l)")
            .set("left", "var(--lumo-space-l)")
            .set("margin", "0")
            .set("position", "absolute");

        HorizontalLayout navigation = getNavigation();
        navigation.getElement();

        myThemeIconHolder = new Div();
        myThemeIconHolder.getStyle().set("font-size", "var(--lumo-font-size-l)")
            .set("right", "var(--lumo-space-l)")
            .set("margin", "0")
            .set("position", "absolute");

        addToNavbar(title, navigation, myThemeIconHolder);
    }

    @Override
    public void onThemeChange(boolean isDark) {
        myThemeIconHolder.removeAll();

        SvgIcon icon = isDark ? LineAwesomeIcon.SUN.create() : LineAwesomeIcon.MOON.create();
        Button changeThemes = new Button(icon);
        changeThemes.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        changeThemes.addSingleClickListener(e -> {
            Page page = UI.getCurrent().getPage();
            boolean dartCurrent = page.getColorScheme() == ColorScheme.Value.DARK;

            if (dartCurrent) {
                page.setColorScheme(ColorScheme.Value.LIGHT);
            } else {
                page.setColorScheme(ColorScheme.Value.DARK);
            }

            ThemeUtil.notifyUpdate();
        });

        myThemeIconHolder.add(changeThemes);
    }

    private HorizontalLayout getNavigation() {
        HorizontalLayout navigation = new HorizontalLayout();
        navigation.addClassNames(LumoUtility.JustifyContent.CENTER,
            LumoUtility.Gap.SMALL, LumoUtility.Height.MEDIUM,
            LumoUtility.Width.FULL);
        navigation.add(createLink(IndexView.class));
        return navigation;
    }

    private RouterLink createLink(Class<? extends Component> viewClass) {
        RouterLink link = new RouterLink("Home", viewClass);

        link.addClassNames(LumoUtility.Display.FLEX,
            LumoUtility.AlignItems.CENTER,
            LumoUtility.Padding.Horizontal.MEDIUM,
            LumoUtility.TextColor.SECONDARY, LumoUtility.FontWeight.MEDIUM);
        link.getStyle().set("text-decoration", "none");

        return link;
    }
}
