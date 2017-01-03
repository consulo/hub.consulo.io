package consulo.webService.ui.install;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;
import com.intellij.util.SystemProperties;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import consulo.webService.UserConfigurationService;
import consulo.webService.ui.util.TidyComponents;
import consulo.webService.ui.util.VaadinUIUtil;
import consulo.webService.util.PropertyKeys;

/**
 * @author VISTALL
 * @since 09-Nov-16
 */
public class Installer
{
	private VerticalLayout myComponent;

	public Installer(UserConfigurationService configurationService, UI ui)
	{
		ui.getPage().setTitle("Installer");

		myComponent = new VerticalLayout();
		myComponent.setSizeFull();

		VerticalLayout layout = new VerticalLayout();
		layout.setSizeUndefined();
		layout.setSpacing(true);

		List<Consumer<Properties>> consumers = new ArrayList<>();

		layout.addComponent(buildCaptchaLayout(consumers));
		layout.addComponent(buildEtcLayout(consumers));

		Button installButton = TidyComponents.newButton("Install");
		installButton.addClickListener(event -> {
			Properties properties = new Properties();

			for(Consumer<Properties> consumer : consumers)
			{
				consumer.accept(properties);
			}

			configurationService.setProperties(properties);
			ui.getPage().reload();
		});
		installButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

		layout.addComponent(installButton);
		layout.setComponentAlignment(installButton, Alignment.MIDDLE_RIGHT);

		myComponent.addComponent(layout);
		myComponent.setComponentAlignment(layout, Alignment.MIDDLE_CENTER);
	}

	public Component build()
	{
		return myComponent;
	}

	@NotNull
	private VerticalLayout buildEtcLayout(List<Consumer<Properties>> consumers)
	{
		VerticalLayout etcLayout = new VerticalLayout();
		etcLayout.setSpacing(true);
		etcLayout.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);

		TextField workingDirectoryField = TidyComponents.newTextField();
		workingDirectoryField.setValue(SystemProperties.getUserHome() + File.separatorChar + ".consuloWebservice");
		etcLayout.addComponent(VaadinUIUtil.labeled("Working directory: ", workingDirectoryField));

		TextField deployKeyField = TidyComponents.newTextField();
		etcLayout.addComponent(VaadinUIUtil.labeled("Deploy key: ", deployKeyField));

		consumers.add(properties -> {
			properties.setProperty(PropertyKeys.WORKING_DIRECTORY, workingDirectoryField.getValue());
			properties.setProperty(PropertyKeys.DEPLOY_KEY, deployKeyField.getValue());
		});
		return etcLayout;
	}

	@NotNull
	private VerticalLayout buildCaptchaLayout(List<Consumer<Properties>> consumers)
	{
		VerticalLayout captchaLayout = new VerticalLayout();
		captchaLayout.setSpacing(true);
		captchaLayout.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);

		CheckBox enabledCaptcha = TidyComponents.newCheckBox("Enable captcha?");
		enabledCaptcha.setValue(true);
		captchaLayout.addComponent(enabledCaptcha);

		TextField privateApiKey = TidyComponents.newTextField();
		captchaLayout.addComponent(VaadinUIUtil.labeled("Private captcha key: ", privateApiKey));

		TextField siteApiKey = TidyComponents.newTextField();
		captchaLayout.addComponent(VaadinUIUtil.labeled("Site captcha key: ", siteApiKey));

		enabledCaptcha.addValueChangeListener(event -> {
			privateApiKey.setEnabled((Boolean) event.getProperty().getValue());
			siteApiKey.setEnabled((Boolean) event.getProperty().getValue());
		});

		consumers.add(properties -> {
			if(enabledCaptcha.getValue())
			{
				properties.setProperty(PropertyKeys.CAPTCHA_ENABLED, "true");
				properties.setProperty(PropertyKeys.CAPTCHA_SITE_KEY, siteApiKey.getValue());
				properties.setProperty(PropertyKeys.CAPTCHA_PRIVATE_KEY, privateApiKey.getValue());
			}
		});
		return captchaLayout;
	}
}
