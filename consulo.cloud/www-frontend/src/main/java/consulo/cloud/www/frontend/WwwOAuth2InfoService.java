package consulo.cloud.www.frontend;

import consulo.procoeton.core.OAuth2InfoService;
import jakarta.annotation.Nonnull;
import org.springframework.stereotype.Component;

/**
 * @author VISTALL
 * @since 2025-05-15
 */
@Component
public class WwwOAuth2InfoService implements OAuth2InfoService {
    @Nonnull
    @Override
    public String getClientName() {
        return "www.cloud";
    }
}
