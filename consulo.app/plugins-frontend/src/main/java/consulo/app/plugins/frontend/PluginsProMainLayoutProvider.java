package consulo.app.plugins.frontend;

import com.vaadin.flow.router.RouterLayout;
import consulo.app.plugins.frontend.ui.PluginsAppLayout;
import consulo.procoeton.core.vaadin.service.ProMainLayoutProvider;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2025-05-10
 */
@Component
public class PluginsProMainLayoutProvider implements ProMainLayoutProvider {
    @Nonnull
    @Override
    public Class<? extends RouterLayout> getLayoutClass() {
        return PluginsAppLayout.class;
    }
}
