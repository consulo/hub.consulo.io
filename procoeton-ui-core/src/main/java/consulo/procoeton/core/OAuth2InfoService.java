package consulo.procoeton.core;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 09/05/2023
 */
public interface OAuth2InfoService {
    @Nonnull
    String getClientName();
}
