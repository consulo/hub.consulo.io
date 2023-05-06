package consulo.hub.frontend.vflow.repository.view;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import consulo.hub.frontend.vflow.backend.service.BackendRepositoryService;
import consulo.hub.frontend.vflow.base.MainLayout;
import consulo.procoeton.core.vaadin.ui.VChildLayout;
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
		ComponentEventListener<ClickEvent<Button>> listener2 = event -> forceIterate(PluginChannel.nightly, PluginChannel.alpha);
		layout.add(new Button("nightly " + rightArrow() + " alpha", listener2));
		ComponentEventListener<ClickEvent<Button>> listener1 = event -> forceIterate(PluginChannel.alpha, PluginChannel.beta);
		layout.add(new Button("alpha " + rightArrow() + " beta", listener1));
		ComponentEventListener<ClickEvent<Button>> listener = event -> forceIterate(PluginChannel.beta, PluginChannel.release);
		layout.add(new Button("beta " + rightArrow() + " release", listener));

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
