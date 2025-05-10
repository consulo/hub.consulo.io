package consulo.app.plugins.frontend.ui.indexView;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.theme.lumo.LumoUtility;
import consulo.hub.shared.repository.PluginNode;
import consulo.procoeton.core.vaadin.ui.util.VaadinUIUtil;

import java.util.List;

/**
 * @author VISTALL
 * @since 2025-05-10
 */
public class WelcomePluginsPanel extends PluginsPanel {
    public static final int MAX_POPULAR_PLUGINS = 6;

    public static final int MAX_MOST_DOWNLOADED_PLUGINS = 12;

    private VerticalLayout myLayout;

    public WelcomePluginsPanel(List<PluginNode> pluginNodes) {
        myLayout = VaadinUIUtil.newVerticalLayout();

//        myLayout.add(createPluginsHeader("Popular Plugins"));
//        Div popularPlugins = createPluginsDiv();
//        myLayout.add(popularPlugins);
//        for (int i = 0; i < MAX_POPULAR_PLUGINS; i++) {
//            PluginNode node = pluginNodes.get(i);
//
//            popularPlugins.add(new PluginCard(node).getComponent());
//        }

        myLayout.add(createPluginsHeader("Most Downloaded Plugins"));
        Div mostDownloadedPlugins = createPluginsDiv();
        myLayout.add(mostDownloadedPlugins);
        for (int i = 0; i < MAX_MOST_DOWNLOADED_PLUGINS; i++) {
            PluginNode node = pluginNodes.get(i);

            mostDownloadedPlugins.add(new PluginCard(node).getComponent());
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
