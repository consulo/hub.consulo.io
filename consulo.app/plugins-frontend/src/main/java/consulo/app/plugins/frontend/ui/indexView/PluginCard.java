package consulo.app.plugins.frontend.ui.indexView;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.card.CardVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.server.InputStreamFactory;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.theme.lumo.Lumo;
import consulo.app.plugins.frontend.ui.PluginView;
import consulo.hub.shared.repository.PluginNode;
import consulo.procoeton.core.vaadin.ThemeChangeNotifier;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
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
        addThemeVariants(CardVariant.LUMO_COVER_MEDIA, CardVariant.LUMO_HORIZONTAL, CardVariant.LUMO_ELEVATED);
        setHeight(10, Unit.EM);
        setMaxHeight(10, Unit.EM);
        setMaxWidth(MAX_WIDTH, Unit.EM);
        setMinWidth(MAX_WIDTH, Unit.EM);

        updateImage(UI.getCurrent().getElement().getThemeList().contains(Lumo.DARK));

        setTitle(new Div(node.name));

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

        PluginNode node = myNode;

        String iconBytes = isDark ? node.iconDarkBytes : node.iconBytes;
        if (iconBytes == null) {
            iconBytes = node.iconBytes;
        }

        Image image = new Image();

        if (iconBytes == null) {
            image.setSrc(new StreamResource(node.id + ".svg", (InputStreamFactory) () -> {
                return getClass().getResourceAsStream("/images/pluginBig.svg");
            }));
        }
        else {
            byte[] imgBytes = Base64.getDecoder().decode(iconBytes);

            image.setSrc(new StreamResource(node.id + ".svg", (InputStreamFactory) () -> new ByteArrayInputStream(imgBytes)));
        }

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
