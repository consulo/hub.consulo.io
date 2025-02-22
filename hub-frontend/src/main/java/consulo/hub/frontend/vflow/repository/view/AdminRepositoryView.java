package consulo.hub.frontend.vflow.repository.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import consulo.hub.frontend.vflow.backend.service.BackendRepositoryService;
import consulo.hub.frontend.vflow.base.MainLayout;
import consulo.hub.shared.auth.Roles;
import consulo.hub.shared.repository.PluginChannel;
import consulo.procoeton.core.vaadin.ui.VChildLayout;
import consulo.procoeton.core.vaadin.ui.util.VaadinUIUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author VISTALL
 * @since 05-Jan-17
 */
@PageTitle("Admin/Repository")
@Route(value = "admin/repository", layout = MainLayout.class)
@RolesAllowed(Roles.ROLE_SUPERUSER)
public class AdminRepositoryView extends VChildLayout {
    private BackendRepositoryService myBackendRepositoryService;

    @Autowired
    public AdminRepositoryView(BackendRepositoryService backendRepositoryService) {
        myBackendRepositoryService = backendRepositoryService;

        setMargin(false);
        setSpacing(false);
        setSizeFull();

        VerticalLayout rowLayout = VaadinUIUtil.newVerticalLayout();
        add(rowLayout);

        HorizontalLayout layout = VaadinUIUtil.newHorizontalLayout();
        layout.setSpacing(true);
        rowLayout.add(layout);

        layout.add(new Button("nightly " + rightArrow() + " alpha", e -> forceIterate(PluginChannel.nightly, PluginChannel.alpha)));
        layout.add(new Button("alpha " + rightArrow() + " beta", e -> forceIterate(PluginChannel.alpha, PluginChannel.beta)));
        layout.add(new Button("beta " + rightArrow() + " release", e -> forceIterate(PluginChannel.beta, PluginChannel.release)));

        rowLayout.add(new Button("Run Cleanup", e -> myBackendRepositoryService.cleanup()));
    }

    @Nonnull
    private static String rightArrow() {
        return "->";
    }

    private void forceIterate(@Nonnull PluginChannel from, @Nonnull PluginChannel to) {
        myBackendRepositoryService.iteratePlugins(from, to);
    }
}
