package consulo.procoeton.core;

import consulo.procoeton.core.util.PropertySet;
import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 04/05/2023
 */
public interface ProPropertiesService {
    @Nonnull
    PropertySet getPropertySet();

    boolean isInstalled();
}
