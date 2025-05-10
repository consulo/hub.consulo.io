package consulo.app.plugins.frontend;

import consulo.procoeton.core.OAuth2InfoService;
import jakarta.annotation.Nonnull;
import org.springframework.stereotype.Service;

/**
 * @author VISTALL
 * @since 2025-05-10
 */
@Service
public class PluginsOAuth2InfoService implements OAuth2InfoService {
    @Nonnull
    @Override
    public String getClientName() {
        return "plugins.app";
    }
}
