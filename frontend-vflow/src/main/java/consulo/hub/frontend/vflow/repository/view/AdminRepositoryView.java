package consulo.hub.frontend.vflow.repository.view;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import consulo.hub.frontend.vflow.backend.service.BackendRepositoryService;
import consulo.hub.frontend.vflow.base.MainLayout;
import consulo.procoeton.core.vaadin.ui.VChildLayout;
import consulo.procoeton.core.vaadin.ui.util.TinyComponents;
import consulo.procoeton.core.vaadin.ui.util.VaadinUIUtil;
import consulo.hub.shared.auth.Roles;
import consulo.hub.shared.repository.PluginChannel;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 05-Jan-17
 */
@PageTitle("Admin/Repository")
@Route(value = "admin/repository", layout = MainLayout.class)
@RolesAllowed(Roles.ROLE_SUPERUSER)
public class AdminRepositoryView extends VChildLayout
{
	private BackendRepositoryService myBackendRepositoryService;

	@Autowired
	public AdminRepositoryView(BackendRepositoryService backendRepositoryService)
	{
		myBackendRepositoryService = backendRepositoryService;

		setMargin(false);
		setSpacing(false);
		setSizeFull();
		//setDefaultComponentAlignment(Alignment.TOP_LEFT);

		HorizontalLayout layout = VaadinUIUtil.newHorizontalLayout();
		layout.setSpacing(true);
		layout.add(TinyComponents.newButton("nightly " + rightArrow() + " alpha", event -> forceIterate(PluginChannel.nightly, PluginChannel.alpha)));
		layout.add(TinyComponents.newButton("alpha " + rightArrow() + " beta", event -> forceIterate(PluginChannel.alpha, PluginChannel.beta)));
		layout.add(TinyComponents.newButton("beta " + rightArrow() + " release", event -> forceIterate(PluginChannel.beta, PluginChannel.release)));

		add(layout);
	}

	@Nonnull
	private static String rightArrow()
	{
		return "->";
	}

	private void forceIterate(@Nonnull PluginChannel from, @Nonnull PluginChannel to)
	{
		myBackendRepositoryService.iteratePlugins(from, to);
	}
}
