package consulo.procoeton.hub;

import consulo.procoeton.core.OAuth2InfoService;
import jakarta.annotation.Nonnull;
import org.springframework.stereotype.Service;

/**
 * @author VISTALL
 * @since 2023-05-09
 */
@Service
public class HubOAuth2InfoService implements OAuth2InfoService {
    @Nonnull
    @Override
    public String getClientName() {
        return "Hub";
    }
}
