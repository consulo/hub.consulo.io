package consulo.hub.frontend.config.view;

import com.fasterxml.jackson.core.type.TypeReference;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import consulo.hub.frontend.PropertiesService;
import consulo.hub.frontend.backend.BackendRequestor;
import consulo.hub.frontend.base.ui.util.TinyComponents;
import consulo.hub.frontend.base.ui.util.VaadinUIUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * @author VISTALL
 * @since 14-Apr-17
 */
@SpringView(name = AdminConfigView.ID)
public class AdminConfigView extends VerticalLayout implements View
{
	public static final String ID = "adminConfig";

	@Autowired
	private PropertiesService myPropertiesService;

	@Autowired
	private BackendRequestor myBackendRequestor;

	public AdminConfigView()
	{
		setSpacing(false);
		setMargin(false);
		setSizeFull();
	}

	@Override
	public void enter(ViewChangeListener.ViewChangeEvent event)
	{
		removeAllComponents();

		Label label = new Label("Config");
		label.addStyleName("headerMargin");

		ConfigPanel configPanel = new ConfigPanel(myBackendRequestor, myPropertiesService, "Apply", (properties) -> {})
		{
			@Override
			protected void addOthers(VerticalLayout t)
			{
				t.addComponent(buildGroup("Accounts", layout ->
				{
					try
					{
						Map<String, String> map = myBackendRequestor.runRequest("/config/jenkins", Map.of(), new TypeReference<Map<String, String>>()
						{
						});

						for(Map.Entry<String, String> entry : map.entrySet())
						{
							layout.addComponent(VaadinUIUtil.labeledFill(entry.getKey(), TinyComponents.newTextField(entry.getValue())));
						}
					}
					catch(Exception ignored)
					{
					}
				}));

			}
		};
		configPanel.addStyleName("bodyMargin");

		addComponent(label);
		addComponent(configPanel);
		setExpandRatio(configPanel, 1);
	}
}
