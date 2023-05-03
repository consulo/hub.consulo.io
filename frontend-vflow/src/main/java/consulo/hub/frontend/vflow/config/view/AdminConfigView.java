package consulo.hub.frontend.vflow.config.view;

import com.fasterxml.jackson.core.type.TypeReference;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import consulo.hub.frontend.vflow.PropertiesService;
import consulo.hub.frontend.vflow.backend.BackendRequestor;
import consulo.hub.frontend.vflow.base.MainLayout;
import consulo.hub.frontend.vflow.base.VChildLayout;
import consulo.hub.frontend.vflow.base.util.TinyComponents;
import consulo.hub.frontend.vflow.base.util.VaadinUIUtil;
import consulo.hub.shared.auth.Roles;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * @author VISTALL
 * @since 14-Apr-17
 */
@Route(value = "admin/config", layout = MainLayout.class)
@RolesAllowed(Roles.ROLE_SUPERUSER)
@PageTitle("Admin/Config")
public class AdminConfigView extends VChildLayout
{
	@Autowired
	private PropertiesService myPropertiesService;

	@Autowired
	private BackendRequestor myBackendRequestor;

	public AdminConfigView()
	{
	}

	@Override
	public void viewReady(AfterNavigationEvent afterNavigationEvent)
	{
		removeAll();

		ConfigPanel configPanel = new ConfigPanel(myBackendRequestor, myPropertiesService, "Apply", (properties) -> {})
		{
			@Override
			protected void addOthers(VerticalLayout t)
			{
				t.add(buildGroup("Accounts", layout ->
				{
					try
					{
						Map<String, String> map = myBackendRequestor.runRequest("/config/jenkins", Map.of(), new TypeReference<Map<String, String>>()
						{
						});

						for(Map.Entry<String, String> entry : map.entrySet())
						{
							layout.add(VaadinUIUtil.labeledFill(entry.getKey(), TinyComponents.newTextField(entry.getValue())));
						}
					}
					catch(Exception ignored)
					{
					}
				}));

			}
		};

		add(configPanel);
	}
}
