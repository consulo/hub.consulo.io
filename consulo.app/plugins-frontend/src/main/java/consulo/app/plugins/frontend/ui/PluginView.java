package consulo.app.plugins.frontend.ui;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.InputStreamFactory;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility;
import consulo.app.plugins.frontend.backend.PluginsCache;
import consulo.app.plugins.frontend.backend.PluginsCacheService;
import consulo.app.plugins.frontend.service.TagsLocalizeLoader;
import consulo.app.plugins.frontend.ui.indexView.PluginCard;
import consulo.app.plugins.frontend.ui.pluginView.InstallOrDownloadButtonPanel;
import consulo.app.plugins.frontend.ui.urlInfo.ExternalUrl;
import consulo.app.plugins.frontend.ui.urlInfo.PluginUrlInfo;
import consulo.hub.shared.repository.PluginNode;
import consulo.procoeton.core.vaadin.ThemeChangeNotifier;
import consulo.procoeton.core.vaadin.ThemeUtil;
import consulo.procoeton.core.vaadin.ui.Badge;
import consulo.procoeton.core.vaadin.ui.VChildLayout;
import consulo.procoeton.core.vaadin.ui.util.VaadinUIUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Optional;

/**
 * @author VISTALL
 * @see consulo.app.plugins.frontend.PluginsProMainLayoutProvider
 * @since 2025-05-11
 */
@Route(value = "/v/:pluginId/:pluginName?", layout = PluginsAppLayout.class)
@AnonymousAllowed
public class PluginView extends VChildLayout implements ThemeChangeNotifier, HasDynamicTitle {
    public static final String PLUGIN_ID = "pluginId";
    public static final String PLUGIN_NAME = "pluginName";

    private final VerticalLayout myContentLayout;
    private final PluginsCacheService myPluginsCacheService;
    private final TagsLocalizeLoader myTagsLocalizeLoader;

    private final HorizontalLayout myHeaderLayout;
    private final HorizontalLayout myDescriptionLayout;
    private final HorizontalLayout myTagRowLayout;
    private final HorizontalLayout myFooterLayout;
    private final Image myImage;
    private PluginNode myNode;

    private final H1 myNameSpan;
    private final Span myVendorSpan;

    private InstallOrDownloadButtonPanel myInstallOrDownloadButtonPanel;

    private final TabSheet myInfoTabs;

    public PluginView(PluginsCacheService pluginsCacheService, TagsLocalizeLoader tagsLocalizeLoader) {
        myPluginsCacheService = pluginsCacheService;
        myTagsLocalizeLoader = tagsLocalizeLoader;

        myContentLayout = VaadinUIUtil.newVerticalLayout();
        myContentLayout.setMaxWidth(PluginCard.MAX_WIDTH * PluginCard.MAX_COLUMNS + PluginCard.EXTRA_GAP, Unit.EM);
        myContentLayout.getStyle().setAlignSelf(Style.AlignSelf.CENTER);
        myContentLayout.addClassNames(LumoUtility.Border.ALL,
            LumoUtility.BorderRadius.LARGE,
            LumoUtility.BorderColor.CONTRAST_10,
            LumoUtility.Padding.LARGE
        );

        add(myContentLayout);

        myHeaderLayout = new HorizontalLayout();
        myNameSpan = new H1();
        myVendorSpan = new Span();
        myVendorSpan.addClassName(LumoUtility.TextColor.SECONDARY);

        myDescriptionLayout = new HorizontalLayout();
        myDescriptionLayout.addClassNames(LumoUtility.Margin.Top.LARGE, LumoUtility.Margin.Bottom.LARGE);
        myDescriptionLayout.setWidthFull();

        myFooterLayout = new HorizontalLayout();
        myFooterLayout.setWidthFull();

        Div imageHolder = new Div();
        imageHolder.addClassName("plugin-icon-card");

        myImage = new Image();
        myImage.setSrc(new StreamResource("plugin.svg", (InputStreamFactory) () -> getClass().getResourceAsStream("/images/pluginBig.svg")));

        imageHolder.add(myImage);

        VerticalLayout nameAndVendor = VaadinUIUtil.newVerticalLayout();
        nameAndVendor.addClassName(LumoUtility.Padding.Left.LARGE);
        nameAndVendor.add(myNameSpan);
        nameAndVendor.add(myVendorSpan);

        myInstallOrDownloadButtonPanel = new InstallOrDownloadButtonPanel(() -> myNode.id);

        myHeaderLayout.setWidthFull();
        myHeaderLayout.add(imageHolder);
        myHeaderLayout.add(nameAndVendor);
        myHeaderLayout.add(myInstallOrDownloadButtonPanel);

        myInfoTabs = new TabSheet();
        myInfoTabs.setWidthFull();

        myTagRowLayout = new HorizontalLayout();

        myContentLayout.add(myHeaderLayout);
        myContentLayout.add(myTagRowLayout);
        myContentLayout.add(myDescriptionLayout);
        myContentLayout.add(myFooterLayout);
        myContentLayout.add(myInfoTabs);
    }

    @Override
    public void viewReady(AfterNavigationEvent afterNavigationEvent) {
        myDescriptionLayout.removeAll();
        myFooterLayout.removeAll();
        myTagRowLayout.removeAll();

        for (int i = 0; i < myInfoTabs.getTabCount(); i++) {
            Tab tab = myInfoTabs.getTabAt(i);

            myInfoTabs.remove(tab);
        }

        RouteParameters parameters = afterNavigationEvent.getRouteParameters();

        Optional<String> pluginId = parameters.get(PLUGIN_ID);

        myNode = myPluginsCacheService.getPluginsCache().mappped().get(pluginId.get());
        if (myNode == null) {
            return;
        }

        updateImage(myNode, ThemeUtil.isDark());

        String description = StringUtils.defaultString(myNode.description);

        Html html = new Html("<div>" + description + "</div>");
        html.getStyle().setWidth("100%");
        myDescriptionLayout.add(html);

        myNameSpan.setText(myNode.name);
        myVendorSpan.setText(StringUtils.defaultString(myNode.vendor));

        PluginUrlInfo pluginUrlInfo = PluginUrlInfo.of(myNode);
        if (pluginUrlInfo != null) {
            for (ExternalUrl url : pluginUrlInfo.build()) {
                HorizontalLayout layout = VaadinUIUtil.newHorizontalLayout();
                layout.setJustifyContentMode(JustifyContentMode.CENTER);
                layout.add(url.icon().create(), new Span(url.text()));

                Anchor anchor = new Anchor(url.url(), layout);

                myFooterLayout.add(anchor);
            }
        }

        myInfoTabs.add("Comments", VaadinUIUtil.newHorizontalLayout());

        PluginsCache cache = myPluginsCacheService.getPluginsCache();

        if (myNode.dependencies != null && myNode.dependencies.length > 0
            || myNode.optionalDependencies != null && myNode.optionalDependencies.length > 0) {

            UnorderedList list = new UnorderedList();
            if (myNode.dependencies != null) {
                for (String dependency : myNode.dependencies) {
                    PluginNode depPlugin = cache.mappped().get(dependency);
                    if (depPlugin == null) {
                        continue;
                    }

                    list.add(new ListItem(new RouterLink(depPlugin.name, PluginView.class, new RouteParameters(Map.of(PLUGIN_ID, dependency)))));
                }
            }

            if (myNode.optionalDependencies != null) {
                for (String dependency : myNode.optionalDependencies) {
                    PluginNode depPlugin = cache.mappped().get(dependency);
                    if (depPlugin == null) {
                        continue;
                    }

                    HorizontalLayout layout = new HorizontalLayout();
                    layout.setAlignItems(Alignment.CENTER);
                    layout.add(new Badge("optional"));
                    layout.add(new RouterLink(depPlugin.name,
                        PluginView.class,
                        new RouteParameters(Map.of(PLUGIN_ID, dependency))
                    ));

                    list.add(new ListItem(layout));
                }
            }

            myInfoTabs.add("Dependencies", list);
        }

        for (String tag : myNode.tags) {
            myTagRowLayout.add(new Badge(myTagsLocalizeLoader.getTagLocalize(tag)));
        }
    }

    public static String getImageUrl(PluginNode node, boolean isDark) {
        return "/i/" + node.id + "?version=" + node.version + "&dark=" + isDark;
    }

    private void updateImage(PluginNode node, boolean isDark) {
        String icon = getImageUrl(node, isDark);

        myImage.setSrc(icon);

        updateIconJS(UI.getCurrent(), icon);
    }

    public static void updateIconJS(UI ui, String url) {
        ui.getPage().executeJs("""
                  (function() {
                     var link = document.querySelector("link[rel~='icon']");
                  if (link) {
                      link.href = $0;
                  }
                  })()
            """, url);
    }

    @Override
    public void onThemeChange(boolean isDark) {
        updateImage(myNode, isDark);
    }

    @Override
    public String getPageTitle() {
        return myNode == null ? "?" : myNode.name + " Plugin for Consulo (Multi-language IDE)";
    }
}
