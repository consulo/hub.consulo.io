package consulo.app.plugins.frontend.ui.urlInfo;

import org.vaadin.lineawesome.LineAwesomeIcon;

import java.util.List;

/**
 * @author VISTALL
 * @since 2025-05-11
 */
public class GithubPluginUrlInfo implements PluginUrlInfo {
    private final String myUrl;

    public GithubPluginUrlInfo(String url) {
        myUrl = url;
    }

    @Override
    public List<ExternalUrl> build() {
        return List.of(
            new ExternalUrl(LineAwesomeIcon.GITHUB, "Home Page", myUrl),
            new ExternalUrl(LineAwesomeIcon.GITHUB, "Issues Tracker", myUrl + "/issues")
        );
    }
}
