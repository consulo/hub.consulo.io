package consulo.webService.ui;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.annotations.Theme;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import consulo.webService.UserConfigurationService;
import consulo.webService.ui.install.Installer;

/**
 * @author VISTALL
 * @since 09-Nov-16
 */
@SpringUI
// No @Push annotation, we are going to enable it programmatically when the user logs on
@Theme("tests-valo-metro")
@StyleSheet("https://fonts.googleapis.com/css?family=Roboto")
public class RootUI extends UI
{
	@Autowired
	private UserConfigurationService myConfigurationService;

	@Override
	protected void init(VaadinRequest request)
	{
		Page page = getPage();

		page.setTitle("Hub");

		Component component = myConfigurationService.getPropertySet() == null ? new Installer(myConfigurationService, getUI()).build() : buildUI();

		setContent(component);
	}

	@NotNull
	private VerticalLayout buildUI()
	{
		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.setSizeFull();

		HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);

		Button repoButton = new Button("Platform & Plugin Repository");
		repoButton.addClickListener(event -> getUI().getPage().setLocation("repo"));
		layout.addComponent(repoButton);

		Button dashButton = new Button("Dashboard");
		dashButton.addClickListener(event -> getUI().getPage().setLocation("dash"));
		layout.addComponent(dashButton);

		verticalLayout.addComponent(layout);
		verticalLayout.setComponentAlignment(layout, Alignment.MIDDLE_CENTER);
		return verticalLayout;
	}
}
