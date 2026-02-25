package consulo.app.plugins.frontend.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.page.ColorScheme;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.LumoUtility;
import consulo.app.plugins.frontend.backend.PluginsCacheService;
import consulo.app.plugins.frontend.service.TagsLocalizeLoader;
import consulo.app.plugins.frontend.sitemap.SitemapCacheService;
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
    public static final String APPLICATION_LD_JSON = "application/ld+json";

    private final ObjectMapper myObjectMapper;
    private final PluginsCacheService myPluginsCacheService;
    private final TagsLocalizeLoader myTagsLocalizeLoader;
    private final SitemapCacheService mySitemapCacheService;
    private Div myThemeIconHolder;

    public PluginsAppLayout(ObjectMapper objectMapper,
                            PluginsCacheService pluginsCacheService,
                            TagsLocalizeLoader tagsLocalizeLoader,
                            SitemapCacheService sitemapCacheService) {
        myObjectMapper = objectMapper;
        myPluginsCacheService = pluginsCacheService;
        myTagsLocalizeLoader = tagsLocalizeLoader;
        mySitemapCacheService = sitemapCacheService;

        HorizontalLayout navigation = getNavigation();

        myThemeIconHolder = new Div();
        myThemeIconHolder.getStyle().set("font-size", "var(--lumo-font-size-l)")
            .set("right", "var(--lumo-space-l)")
            .set("margin", "0")
            .set("position", "absolute");

        addToNavbar(navigation, myThemeIconHolder);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        onThemeChange(ThemeUtil.isDark());
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        super.beforeEnter(event);

        String json = null;
        if (event.getNavigationTarget() == PluginView.class) {
            String pluginId = event.getRouteParameters().get(PluginView.PLUGIN_ID).orElse(null);
            if (pluginId != null) {
                String jsonLd = myPluginsCacheService.getPluginsCache().getJsonLd(
                    pluginId,
                    myObjectMapper,
                    mySitemapCacheService,
                    myTagsLocalizeLoader
                );

                if (jsonLd != null) {
                    json = jsonLd;
                }
            }
        }

        Element element = UI.getCurrent().getElement();

        Element scriptTag = element.getChildren()
            .filter(c -> "script".equals(c.getTag()) && APPLICATION_LD_JSON.equals(c.getAttribute("type")))
            .findAny()
            .orElse(null);

        if (json == null) {
            if (scriptTag != null) {
                scriptTag.removeFromParent();
            }
        }
        else {
            if (scriptTag != null) {
                scriptTag.setText(json);
            }
            else {

                scriptTag = new Element("script");
                scriptTag.setAttribute("type", APPLICATION_LD_JSON);
                scriptTag.setText(json);

                element.insertChild(0, scriptTag);
            }
        }
    }

    @Override
    public void onThemeChange(boolean isDark) {
        myThemeIconHolder.removeAll();

        SvgIcon icon = isDark ? LineAwesomeIcon.SUN.create() : LineAwesomeIcon.MOON.create();
        Button changeThemes = new Button(icon);
        changeThemes.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        changeThemes.addSingleClickListener(e -> {
            Page page = UI.getCurrent().getPage();

            UI ui = UI.getCurrent();
            boolean dartCurrent = page.getColorScheme() == ColorScheme.Value.DARK;

            if (dartCurrent) {
                page.setColorScheme(ColorScheme.Value.LIGHT);
            }
            else {
                page.setColorScheme(ColorScheme.Value.DARK);
            }

            boolean newDark = !dartCurrent;
            ui.getPage().executeJs(
                "document.cookie = 'darkTheme=' + $0 + ';path=/;max-age=31536000;SameSite=Lax'", newDark);

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
        RouterLink link = new RouterLink("Plugins Repository", viewClass);

        link.addClassNames(LumoUtility.Display.FLEX,
            LumoUtility.AlignItems.CENTER,
            LumoUtility.Padding.Horizontal.MEDIUM,
            LumoUtility.TextColor.SECONDARY, LumoUtility.FontWeight.MEDIUM);
        link.getStyle().set("text-decoration", "none");

        return link;
    }
}
