package consulo.app.plugins.frontend.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.page.ColorScheme;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.RouterLink;
import consulo.app.plugins.frontend.backend.PluginsCacheService;
import consulo.app.plugins.frontend.service.TagsLocalizeLoader;
import consulo.app.plugins.frontend.sitemap.SitemapCacheService;
import consulo.procoeton.core.vaadin.NoMenuAppLayout;
import consulo.procoeton.core.vaadin.ThemeChangeNotifier;
import consulo.procoeton.core.vaadin.ThemeUtil;
import consulo.procoeton.core.vaadin.ui.util.VaadinUIUtil;
import consulo.procoeton.core.vaadin.util.ProcoetonStyles;
import jakarta.annotation.security.PermitAll;
import org.vaadin.lineawesome.LineAwesomeIcon;

/**
 * @author VISTALL
 * @since 2025-05-11
 */
@PermitAll
@PreserveOnRefresh
public class PluginsAppLayout extends NoMenuAppLayout implements ThemeChangeNotifier {
    public static final String APPLICATION_LD_JSON = "application/ld+json";

    private final ObjectMapper myObjectMapper;
    private final PluginsCacheService myPluginsCacheService;
    private final TagsLocalizeLoader myTagsLocalizeLoader;
    private final SitemapCacheService mySitemapCacheService;
    private HorizontalLayout myThemeIconHolder;

    public PluginsAppLayout(ObjectMapper objectMapper,
                            PluginsCacheService pluginsCacheService,
                            TagsLocalizeLoader tagsLocalizeLoader,
                            SitemapCacheService sitemapCacheService) {
        myObjectMapper = objectMapper;
        myPluginsCacheService = pluginsCacheService;
        myTagsLocalizeLoader = tagsLocalizeLoader;
        mySitemapCacheService = sitemapCacheService;

        HorizontalLayout topElement = new HorizontalLayout();
        topElement.setWidthFull();
        topElement.getStyle().setAlignItems(Style.AlignItems.BASELINE);
        topElement.addClassNames(ProcoetonStyles.JustifyContent.CENTER, ProcoetonStyles.Padding.SMALL);
        topElement.add(createLink(IndexView.class));

        myThemeIconHolder = VaadinUIUtil.newHorizontalLayout();

        topElement.add(myThemeIconHolder);

        add(topElement);

        setHeightFull();
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
        changeThemes.addThemeVariants(ButtonVariant.AURA_TERTIARY);
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

    private RouterLink createLink(Class<? extends Component> viewClass) {
        RouterLink link = new RouterLink("Plugins Repository", viewClass);
        link.addClassNames(ProcoetonStyles.FontSize.LARGE, ProcoetonStyles.FontWeight.BOLD);
        link.getStyle().set("text-decoration", "none");

        return link;
    }
}
