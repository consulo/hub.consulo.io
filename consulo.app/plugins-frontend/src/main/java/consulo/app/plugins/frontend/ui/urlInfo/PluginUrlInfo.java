package consulo.app.plugins.frontend.ui.urlInfo;

import consulo.hub.shared.repository.PluginNode;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @author VISTALL
 * @since 2025-05-11
 */
public interface PluginUrlInfo {
    public static PluginUrlInfo of(PluginNode node) {
        if (StringUtils.isBlank(node.url)) {
            return null;
        }

        if (node.url.startsWith("https://github.com")) {
            return new GithubPluginUrlInfo(node.url);
        }

        return new SimplePluginUrlInfo(node.url);
    }

    List<ExternalUrl> build();
}
