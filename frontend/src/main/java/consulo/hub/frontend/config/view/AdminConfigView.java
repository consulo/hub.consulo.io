package consulo.hub.frontend.config.view;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import consulo.hub.frontend.PropertiesService;
import consulo.hub.frontend.backend.BackendRequestor;
import org.springframework.beans.factory.annotation.Autowired;

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

		ConfigPanel configPanel = new ConfigPanel(myBackendRequestor, myPropertiesService, "Apply", (properties) -> {});
		configPanel.addStyleName("bodyMargin");

		addComponent(label);
		addComponent(configPanel);
		setExpandRatio(configPanel, 1);
	}
}
