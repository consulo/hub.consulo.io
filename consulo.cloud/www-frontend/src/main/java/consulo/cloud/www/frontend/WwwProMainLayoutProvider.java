package consulo.cloud.www.frontend;

import com.vaadin.flow.router.RouterLayout;
import consulo.procoeton.core.vaadin.SimpleAppLayout;
import consulo.procoeton.core.vaadin.service.ProMainLayoutProvider;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2025-05-15
 */
@Component
public class WwwProMainLayoutProvider implements ProMainLayoutProvider {
    @Nonnull
    @Override
    public Class<? extends RouterLayout> getLayoutClass() {
        return SimpleAppLayout.class;
    }
}
