package consulo.app.plugins.frontend.ui;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import consulo.app.plugins.frontend.backend.PluginsCacheService;
import consulo.app.plugins.frontend.ui.indexView.PluginCard;
import consulo.app.plugins.frontend.ui.indexView.SearchPluginPanel;
import consulo.app.plugins.frontend.ui.indexView.WelcomePluginsPanel;
import consulo.procoeton.core.vaadin.ui.VChildLayout;
import consulo.procoeton.core.vaadin.ui.util.VaadinUIUtil;
import org.apache.commons.lang3.StringUtils;
import org.vaadin.lineawesome.LineAwesomeIcon;

/**
 * @author VISTALL
 * @since 2025-05-10
 */
@Route(value = "/", layout = PluginsAppLayout.class)
@AnonymousAllowed
public class IndexView extends VChildLayout {
    private final WelcomePluginsPanel myWelcomePanel;

    private final SearchPluginPanel mySearchPluginPanel;

    private final PluginsCacheService myPluginsCacheService;

    public IndexView(PluginsCacheService pluginsCacheService) {
        myPluginsCacheService = pluginsCacheService;

        HorizontalLayout searchLayout = VaadinUIUtil.newHorizontalLayout();
        searchLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        searchLayout.setWidthFull();

        myWelcomePanel = new WelcomePluginsPanel(myPluginsCacheService);
        mySearchPluginPanel = new SearchPluginPanel(myPluginsCacheService);

        VerticalLayout holder = VaadinUIUtil.newVerticalLayout();

        TextField searchField = new TextField();
        searchField.setAutofocus(true);
        searchField.addThemeVariants(TextFieldVariant.LUMO_ALIGN_CENTER);
        searchField.setPrefixComponent(LineAwesomeIcon.SEARCH_SOLID.create());
        searchField.setPlaceholder("Searching Plugin by Name or Tag...");
        searchField.setMinWidth(PluginCard.MAX_WIDTH, Unit.EM);
        searchField.setMaxWidth(PluginCard.MAX_WIDTH * PluginCard.MAX_COLUMNS, Unit.EM);
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.getStyle().setAlignSelf(Style.AlignSelf.CENTER);
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

        searchLayout.addAndExpand(searchField);

        holder.setWidthFull();
        holder.add(searchLayout);
        add(holder);

        holder.add(myWelcomePanel.getComponent());
    }
}
