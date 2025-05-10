package consulo.app.plugins.frontend.ui.indexView;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import consulo.hub.shared.repository.PluginNode;
import consulo.procoeton.core.vaadin.ui.util.VaadinUIUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

import static consulo.app.plugins.frontend.ui.indexView.WelcomePluginsPanel.createPluginsDiv;

/**
 * @author VISTALL
 * @since 2025-05-10
 */
public class SearchPluginPanel extends PluginsPanel {
    private final VerticalLayout myLayout;
    private final List<PluginNode> myPluginNodes;

    public SearchPluginPanel(List<PluginNode> pluginNodes) {
        myPluginNodes = pluginNodes;
        myLayout = VaadinUIUtil.newVerticalLayout();
        myLayout.add(WelcomePluginsPanel.createPluginsHeader("Search Result"));
    }

    public void updatePlugins(String searchStr) {
        List<PluginNode> nodes = myPluginNodes.parallelStream().filter(node -> isAccepted(node, searchStr)).toList();

        myLayout.removeAll();
        myLayout.add(WelcomePluginsPanel.createPluginsHeader("Found " + nodes.size() + " Plugins"));

        Div pluginsContainer = createPluginsDiv();
        myLayout.add(pluginsContainer);

        int i = 0;
        for (PluginNode node : nodes) {
            if (i++ > 20) {
                break;
            }

            pluginsContainer.add(new PluginCard(node).getComponent());
        }

        UI.getCurrent().push();
    }

    private boolean isAccepted(PluginNode pluginNode, String searchStr) {
        String name = pluginNode.name;
        if (StringUtils.containsIgnoreCase(name, searchStr)) {
            return true;
        }


        String description = StringUtils.defaultString(pluginNode.description);
        if (StringUtils.containsIgnoreCase(description, searchStr)) {
            return true;
        }

        String vendor = StringUtils.defaultString(pluginNode.vendor);
        if (StringUtils.containsIgnoreCase(vendor, searchStr)) {
            return true;
        }
        return false;
    }

    @Override
    public Component getComponent() {
        return myLayout;
    }
}
