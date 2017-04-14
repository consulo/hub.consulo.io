package consulo.webService.config.view;

import org.springframework.beans.factory.annotation.Autowired;
import com.intellij.openapi.util.EmptyRunnable;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import consulo.webService.UserConfigurationService;

/**
 * @author VISTALL
 * @since 14-Apr-17
 */
@SpringView(name = AdminConfigView.ID)
public class AdminConfigView extends VerticalLayout implements View
{
	public static final String ID = "adminConfig";

	@Autowired
	private UserConfigurationService myUserConfigurationService;

	public AdminConfigView()
	{
		setSizeFull();
	}

	@Override
	public void enter(ViewChangeListener.ViewChangeEvent event)
	{
		removeAllComponents();

		Label label = new Label("Config");
		label.addStyleName("headerMargin");

		ConfigPanel configPanel = new ConfigPanel(myUserConfigurationService, "Apply", EmptyRunnable.getInstance());
		configPanel.addStyleName("bodyMargin");

		addComponent(label);
		addComponent(configPanel);
		setExpandRatio(configPanel, 1);
	}
}
