package consulo.app.plugins.frontend.backend;

import jakarta.annotation.Nonnull;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author VISTALL
 * @since 2025-05-13
 */
@Service
public class FeaturePluginsService {
    private final List<String> FEATURED_PLUGINS = List.of(
        "consulo.csharp",
        "consulo.java",
        "consulo.javascript",
        "consulo.unity3d"
    );

    @Nonnull
    public List<String> getFeaturedPlugins() {
        return FEATURED_PLUGINS;
    }
}
