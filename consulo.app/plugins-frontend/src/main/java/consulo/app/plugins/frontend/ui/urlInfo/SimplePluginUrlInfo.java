package consulo.app.plugins.frontend.ui.urlInfo;

import org.vaadin.lineawesome.LineAwesomeIcon;

import java.util.List;

/**
 * @author VISTALL
 * @since 2025-05-11
 */
public class SimplePluginUrlInfo implements PluginUrlInfo {
    private final String myUrl;

    public SimplePluginUrlInfo(String url) {
        myUrl = url;
    }

    @Override
    public List<ExternalUrl> build() {
        return List.of(new ExternalUrl(LineAwesomeIcon.EXTERNAL_LINK_ALT_SOLID, "Home Page", myUrl));
    }
}
