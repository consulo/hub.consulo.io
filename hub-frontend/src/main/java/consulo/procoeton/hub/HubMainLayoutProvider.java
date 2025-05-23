package consulo.procoeton.hub;

import com.vaadin.flow.router.RouterLayout;
import consulo.hub.frontend.vflow.base.MainLayout;
import consulo.procoeton.core.vaadin.service.ProMainLayoutProvider;
import jakarta.annotation.Nonnull;
import org.springframework.stereotype.Component;

/**
 * @author VISTALL
 * @since 2023-05-04
 */
@Component
public class HubMainLayoutProvider implements ProMainLayoutProvider {
    @Nonnull
    @Override
    public Class<? extends RouterLayout> getLayoutClass() {
        return MainLayout.class;
    }
}
