package consulo.app.plugins.frontend.ui.indexView;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.theme.lumo.LumoUtility;
import consulo.app.plugins.frontend.backend.FeaturePluginsService;
import consulo.app.plugins.frontend.backend.PluginsCache;
import consulo.app.plugins.frontend.backend.PluginsCacheService;
import consulo.hub.shared.repository.PluginNode;
import consulo.procoeton.core.vaadin.ui.util.VaadinUIUtil;

import java.util.List;

/**
 * @author VISTALL
 * @since 2025-05-10
 */
public class WelcomePluginsPanel extends PluginsPanel {
    public static final int MAX_MOST_DOWNLOADED_PLUGINS = 12;
    private final PluginsCacheService myPluginsCacheService;
    private final FeaturePluginsService myFeaturePluginsService;

    private VerticalLayout myLayout;

    public WelcomePluginsPanel(PluginsCacheService pluginsCacheService,
                               FeaturePluginsService featurePluginsService) {
        myPluginsCacheService = pluginsCacheService;
        myFeaturePluginsService = featurePluginsService;
        myLayout = VaadinUIUtil.newVerticalLayout();
    }

    public void viewReady() {
        myLayout.removeAll();

        PluginsCache cache = myPluginsCacheService.getPluginsCache();

        List<String> featuredPlugins = myFeaturePluginsService.getFeaturedPlugins();
        if (!featuredPlugins.isEmpty()) {
            myLayout.add(createPluginsHeader("Featured Plugins"));

            Div featuredDivHolder = createPluginsDiv();

            myLayout.add(featuredDivHolder);

            for (String featuredPlugin : featuredPlugins) {
                PluginNode node = cache.mappped().get(featuredPlugin);

                featuredDivHolder.add(new PluginCard(node));
            }
        }

        myLayout.add(createPluginsHeader("Most Downloaded Plugins"));

        Div downloadedPlugins = createPluginsDiv();
        myLayout.add(downloadedPlugins);

        for (int i = 0; i < MAX_MOST_DOWNLOADED_PLUGINS; i++) {
            PluginNode node = cache.sortedByDownloads().get(i);

            downloadedPlugins.add(new PluginCard(node));
        }
    }

    public static Component createPluginsHeader(String str) {
        Div holder = new Div();
        holder.addClassName("plugins-header-header-data");

        Div left = new Div();
        holder.add(left);

        Span header = new Span(str);
        header.addClassNames(LumoUtility.Margin.LARGE,
            LumoUtility.Padding.MEDIUM,
            LumoUtility.TextColor.SECONDARY,
            LumoUtility.FontSize.MEDIUM,
            LumoUtility.FontWeight.SEMIBOLD
        );
        holder.add(header);
        holder.setWidthFull();

        Div right = new Div();
        holder.add(right);

        return holder;
    }

    public static Div createPluginsDiv() {
        Div div = new Div();
        div.addClassName("plugins-card-holder");
        div.setMaxWidth(PluginCard.MAX_WIDTH * PluginCard.MAX_COLUMNS + PluginCard.EXTRA_GAP, Unit.EM);
        div.getStyle().setAlignSelf(Style.AlignSelf.CENTER);
        return div;
    }

    @Override
    public Component getComponent() {
        return myLayout;
    }
}
