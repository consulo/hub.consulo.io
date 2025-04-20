package consulo.procoeton.hub;

import com.vaadin.flow.router.RouterLayout;
import consulo.hub.frontend.vflow.base.MainLayout;
import consulo.procoeton.core.vaadin.service.ProMainLayoutProvider;
import org.springframework.stereotype.Service;

/**
 * @author VISTALL
 * @since 04/05/2023
 */
@Service
public class HubMainLayoutProvider implements ProMainLayoutProvider {
    @Override
    public Class<? extends RouterLayout> getLayoutClass() {
        return MainLayout.class;
    }
}
