package consulo.app.plugins.frontend.ui.indexView;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.card.CardVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.router.RouteParameters;
import consulo.app.plugins.frontend.ui.PluginView;
import consulo.hub.shared.repository.PluginNode;
import consulo.procoeton.core.vaadin.ThemeChangeNotifier;
import consulo.procoeton.core.vaadin.ThemeUtil;
import consulo.procoeton.core.vaadin.util.ProcoetonStyles;
import org.apache.commons.lang3.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author VISTALL
 * @since 2025-05-10
 */
public class PluginCard extends Card implements ClickNotifier<PluginCard>, ThemeChangeNotifier {
    public static final int MAX_WIDTH = 30;

    public static final int EXTRA_GAP = 10;

    public static final int MAX_COLUMNS = 3;

    private final PluginNode myNode;

    public PluginCard(PluginNode node) {
        myNode = node;
        addClassName("plugin-card");
        addThemeVariants(CardVariant.LUMO_COVER_MEDIA, CardVariant.LUMO_HORIZONTAL, CardVariant.LUMO_ELEVATED);
        setHeight(8, Unit.EM);
        setMaxHeight(8, Unit.EM);
        setMaxWidth(MAX_WIDTH, Unit.EM);
        setMinWidth(MAX_WIDTH, Unit.EM);

        updateImage(ThemeUtil.isDark());

        Div title = new Div(node.name);
        title.addClassName(ProcoetonStyles.FontSize.XLARGE);
        setTitle(title);

        if (!StringUtils.isBlank(node.vendor)) {
            setSubtitle(new Div(node.vendor));
        }

        String text = StringUtils.defaultString(node.description);

        Html html = new Html("<div>" + text + "</div>");
        html.addClassName("plugin-card-description");

        add(html);

        getStyle().setCursor("pointer");

        addSingleClickListener(pluginCardClickEvent -> {
            UI.getCurrent().navigate(PluginView.class, new RouteParameters(Map.of(PluginView.PLUGIN_ID, node.id,
                PluginView.PLUGIN_NAME, URLEncoder.encode(node.name, StandardCharsets.UTF_8)))
            );
        });
    }

    public void updateImage(boolean isDark) {
        Div imageHolder = new Div();
        imageHolder.addClassName("plugin-icon-card");

        Image image = new Image();
        image.setSrc("/i/" + myNode.id + "?version=" + myNode.version + "&dark=" + isDark);
        imageHolder.add(image);

        setMedia(imageHolder);
    }

    public Component getComponent() {
        return this;
    }

    @Override
    public void onThemeChange(boolean isDark) {
        updateImage(isDark);
    }
}
