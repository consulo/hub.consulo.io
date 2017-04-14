package consulo.webService.config.view;

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
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import consulo.webService.UserConfigurationService;
import consulo.webService.github.GithubPropertyKeys;
import consulo.webService.ui.util.TidyComponents;
import consulo.webService.ui.util.VaadinUIUtil;
import consulo.webService.util.PropertyKeys;

/**
 * @author VISTALL
 * @since 14-Apr-17
 */
public class ConfigPanel extends VerticalLayout
{
	public ConfigPanel(@NotNull UserConfigurationService configurationService, @NotNull String buttonName, @NotNull Runnable action)
	{
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeUndefined();
		layout.setSpacing(true);
		layout.setSizeFull();

		List<Consumer<Properties>> consumers = new ArrayList<>();

		layout.addComponent(buildRepositoryGroup(consumers));
		layout.addComponent(buildCaptchaGroup(consumers));
		layout.addComponent(buildGithubGroup(consumers));

		Button installButton = TidyComponents.newButton(buttonName);
		installButton.addClickListener(event ->
		{
			Properties properties = new Properties();

			for(Consumer<Properties> consumer : consumers)
			{
				consumer.accept(properties);
			}

			configurationService.setProperties(properties);

			action.run();
		});
		installButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

		layout.addComponent(installButton);
		layout.setComponentAlignment(installButton, Alignment.MIDDLE_RIGHT);

		addComponent(layout);
		setComponentAlignment(layout, Alignment.MIDDLE_CENTER);
	}

	@NotNull
	private Component buildRepositoryGroup(List<Consumer<Properties>> consumers)
	{
		return createGroup("Repository", layout ->
		{
			TextField workingDirectoryField = TidyComponents.newTextField();
			workingDirectoryField.setValue(SystemProperties.getUserHome() + File.separatorChar + ".consuloWebservice");
			layout.addComponent(VaadinUIUtil.labeledFill("Working directory: ", workingDirectoryField));

			TextField deployKeyField = TidyComponents.newTextField();
			layout.addComponent(VaadinUIUtil.labeledFill("Deploy key: ", deployKeyField));

			consumers.add(properties ->
			{
				properties.setProperty(PropertyKeys.WORKING_DIRECTORY, workingDirectoryField.getValue());
				properties.setProperty(PropertyKeys.DEPLOY_KEY, deployKeyField.getValue());
			});
		});
	}

	@NotNull
	private Component buildCaptchaGroup(List<Consumer<Properties>> consumers)
	{
		return createGroup("Captcha", layout ->
		{
			CheckBox enabledCaptcha = TidyComponents.newCheckBox("Enable captcha?");
			enabledCaptcha.setValue(true);
			layout.addComponent(enabledCaptcha);

			TextField privateApiKey = TidyComponents.newTextField();
			layout.addComponent(VaadinUIUtil.labeledFill("Private captcha key: ", privateApiKey));

			TextField siteApiKey = TidyComponents.newTextField();
			layout.addComponent(VaadinUIUtil.labeledFill("Site captcha key: ", siteApiKey));

			enabledCaptcha.addValueChangeListener(event ->
			{
				privateApiKey.setEnabled((Boolean) event.getProperty().getValue());
				siteApiKey.setEnabled((Boolean) event.getProperty().getValue());
			});
			enabledCaptcha.setValue(false);

			consumers.add(properties ->
			{
				if(enabledCaptcha.getValue())
				{
					properties.setProperty(PropertyKeys.CAPTCHA_ENABLED, "true");
					properties.setProperty(PropertyKeys.CAPTCHA_SITE_KEY, siteApiKey.getValue());
					properties.setProperty(PropertyKeys.CAPTCHA_PRIVATE_KEY, privateApiKey.getValue());
				}
			});
		});
	}

	@NotNull
	private Component buildGithubGroup(List<Consumer<Properties>> consumers)
	{
		return createGroup("Github", layout ->
		{
			TextField oauthKeyField = TidyComponents.newTextField();
			layout.addComponent(VaadinUIUtil.labeledFill("OAuth Key: ", oauthKeyField));

			TextField secretHookKeyField = TidyComponents.newTextField();
			layout.addComponent(VaadinUIUtil.labeledFill("Secret Hook Key: ", secretHookKeyField));

			consumers.add(properties ->
			{
				properties.setProperty(GithubPropertyKeys.OAUTH_KEY, oauthKeyField.getValue());
				properties.setProperty(GithubPropertyKeys.SECRET_HOOK_KEY, secretHookKeyField.getValue());
			});
		});
	}

	@NotNull
	private static Panel createGroup(String title, Consumer<VerticalLayout> consumer)
	{
		Panel panel = new Panel(title);
		panel.setSizeFull();

		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.setMargin(true);
		verticalLayout.setSpacing(true);
		verticalLayout.setSizeFull();
		verticalLayout.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);

		panel.setContent(verticalLayout);

		consumer.accept(verticalLayout);
		return panel;
	}
}
