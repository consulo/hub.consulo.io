package consulo.app.plugins.frontend.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.dom.ThemeList;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.Lumo;
import com.vaadin.flow.theme.lumo.LumoUtility;
import consulo.app.plugins.frontend.backend.PluginsCacheService;
import consulo.app.plugins.frontend.service.TagsLocalizeLoader;
import consulo.app.plugins.frontend.sitemap.SitemapCacheService;
import consulo.procoeton.core.vaadin.SimpleAppLayout;
import consulo.procoeton.core.vaadin.ThemeChangeNotifier;
import consulo.procoeton.core.vaadin.ThemeUtil;
import org.vaadin.lineawesome.LineAwesomeIcon;

/**
 * @author VISTALL
 * @since 2025-05-11
 */
@PreserveOnRefresh
public class PluginsAppLayout extends SimpleAppLayout implements ThemeChangeNotifier {
    private static final String REMOVE_LD_JS =
        """
            let node = document.querySelector('head script[type="application/ld+json"]');
            if (node) {
                node.remove();
            }
            """;

    private static final String INSERT_LD_JS = """
            let node = document.querySelector('head script[type="application/ld+json"]');

            if (!node) {
                node = document.createElement('script');
                node.type = 'application/ld+json';
                document.head.appendChild(node);
            }
            node.textContent = $0;
        """;

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
    public void beforeEnter(BeforeEnterEvent event) {
        super.beforeEnter(event);

        String js = REMOVE_LD_JS;
        String json = "";
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
                    js = INSERT_LD_JS;
                    json = jsonLd;
                }
            }
        }

        UI.getCurrent().getPage().executeJs(js, json);
    }

    @Override
    public void onThemeChange(boolean isDark) {
        myThemeIconHolder.removeAll();

        SvgIcon icon =
            isDark ? LineAwesomeIcon.SUN.create() : LineAwesomeIcon.MOON.create();
        Button changeThemes = new Button(icon);
        changeThemes.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        changeThemes.addSingleClickListener(e -> {
            ThemeList themeList = UI.getCurrent().getElement().getThemeList();
            boolean dartCurrent = themeList.contains(Lumo.DARK);

            if (dartCurrent) {
                themeList.remove(Lumo.DARK);
                themeList.add(Lumo.LIGHT);
            }
            else {
                themeList.remove(Lumo.LIGHT);
                themeList.add(Lumo.DARK);
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
