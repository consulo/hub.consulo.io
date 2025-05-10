package consulo.app.plugins.frontend.ui;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import consulo.app.plugins.frontend.backend.service.BackendRepositoryService;
import consulo.app.plugins.frontend.ui.indexView.PluginCard;
import consulo.app.plugins.frontend.ui.indexView.PluginsPanel;
import consulo.app.plugins.frontend.ui.indexView.SearchPluginPanel;
import consulo.app.plugins.frontend.ui.indexView.WelcomePluginsPanel;
import consulo.hub.shared.repository.PluginNode;
import consulo.hub.shared.repository.util.RepositoryUtil;
import consulo.procoeton.core.vaadin.ui.VChildLayout;
import consulo.procoeton.core.vaadin.ui.util.VaadinUIUtil;
import org.apache.commons.lang3.StringUtils;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @since 2025-05-10
 */
@Route(value = "/", layout = PluginsAppLayout.class)
@AnonymousAllowed
public class IndexView extends VChildLayout {
    private final BackendRepositoryService myBackendRepositoryService;

    private final PluginsPanel myWelcomePanel;

    private final SearchPluginPanel mySearchPluginPanel;

    public IndexView(BackendRepositoryService backendRepositoryService) {
        myBackendRepositoryService = backendRepositoryService;

        HorizontalLayout searchLayout = VaadinUIUtil.newHorizontalLayout();
        searchLayout.setWidthFull();

        PluginNode[] pluginNodes = myBackendRepositoryService.listOldPlugins();

        List<PluginNode> list = new ArrayList<>(List.of(pluginNodes));
        list.removeIf(node -> RepositoryUtil.isPlatformNode(node.id));
        list.sort((o1, o2) -> Integer.compareUnsigned(o2.downloads, o1.downloads));

        myWelcomePanel = new WelcomePluginsPanel(list);
        mySearchPluginPanel = new SearchPluginPanel(list);

        VerticalLayout holder = VaadinUIUtil.newVerticalLayout();

        TextField searchField = new TextField();
        searchField.addThemeVariants(TextFieldVariant.LUMO_ALIGN_CENTER);
        searchField.setPrefixComponent(LineAwesomeIcon.SEARCH_SOLID.create());
        searchField.setPlaceholder("Searching Plugin by Name or Tag...");
        searchField.setWidthFull();
        searchField.setMaxWidth(PluginCard.MAX_WIDTH * PluginCard.MAX_COLUMNS, Unit.EM);
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(e -> {
            if (StringUtils.isBlank(e.getValue())) {
                mySearchPluginPanel.getComponent().removeFromParent();

                if (myWelcomePanel.getComponent().getParent().isEmpty()) {
                    holder.add(myWelcomePanel.getComponent());
                }
            } else {
                myWelcomePanel.getComponent().removeFromParent();

                mySearchPluginPanel.updatePlugins(e.getValue());

                if (mySearchPluginPanel.getComponent().getParent().isEmpty()) {
                    holder.add(mySearchPluginPanel.getComponent());
                }
            }
        });

        searchLayout.add(searchField);

        holder.setWidthFull();
        holder.add(searchLayout);
        add(holder);

        holder.add(myWelcomePanel.getComponent());
    }
}
