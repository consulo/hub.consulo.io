package consulo.hub.frontend.config.view;

import com.fasterxml.jackson.core.type.TypeReference;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import consulo.hub.frontend.PropertiesService;
import consulo.hub.frontend.backend.BackendRequestor;
import consulo.hub.frontend.base.ui.util.TinyComponents;
import consulo.hub.frontend.base.ui.util.VaadinUIUtil;
import consulo.hub.frontend.util.GAPropertyKeys;
import consulo.hub.frontend.util.PropertyKeys;
import consulo.hub.frontend.util.PropertySet;
import consulo.hub.shared.github.GithubPropertyKeys;
import org.apache.commons.lang3.StringUtils;
import sun.jvm.hotspot.debugger.Page;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author VISTALL
 * @since 14-Apr-17
 */
public class ConfigPanel extends VerticalLayout
{
	private final List<Consumer<Properties>> myConsumers = new ArrayList<>();
	private final PropertiesService myConfigurationService;

	public ConfigPanel(BackendRequestor backendRequestor, PropertiesService configurationService, String buttonName, Consumer<Properties> action)
	{
		myConfigurationService = configurationService;
		setSpacing(false);
		setMargin(false);

		VerticalLayout layout = VaadinUIUtil.newVerticalLayout();
		layout.setSizeUndefined();
		layout.setSpacing(true);
		layout.setSizeFull();

		layout.addComponent(buildBackedGroup(backendRequestor));
		layout.addComponent(buildCaptchaGroup());
		layout.addComponent(buildGithubGroup());
		layout.addComponent(buildGAGroup());

		Button installButton = TinyComponents.newButton(buttonName);
		installButton.addClickListener(event ->
		{
			Properties properties = new Properties();

			for(Consumer<Properties> consumer : myConsumers)
			{
				consumer.accept(properties);
			}

			configurationService.setProperties(properties);

			action.accept(properties);
		});
		installButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

		addOthers(layout);

		layout.addComponent(installButton);
		layout.setComponentAlignment(installButton, Alignment.MIDDLE_RIGHT);

		addComponent(layout);
		setComponentAlignment(layout, Alignment.MIDDLE_CENTER);
	}

	protected void addOthers(VerticalLayout layout)
	{
	}

	@SuppressWarnings("unchecked")
	private <T> void map(Class<T> clazz, HasValue<T> property, String key, Supplier<T> defSupplier)
	{
		myConsumers.add(properties ->
		{
			String value = StringUtils.stripToNull(String.valueOf(property.getValue()));
			if(value == null)
			{
				properties.remove(key);
				return;
			}
			properties.setProperty(key, value);
		});

		if(myConfigurationService.isNotInstalled())
		{
			if(defSupplier != null)
			{
				property.setValue(defSupplier.get());
			}
		}
		else
		{
			PropertySet propertySet = myConfigurationService.getPropertySet();

			String value = propertySet.getStringProperty(key);
			if(value != null)
			{
				if(clazz == Boolean.class)
				{
					property.setValue((T) Boolean.valueOf(value));
				}
				else if(clazz == String.class)
				{
					property.setValue((T) value);
				}
				else
				{
					throw new IllegalArgumentException(clazz.getName());
				}
			}
		}
	}

	private Component buildBackedGroup(BackendRequestor backendRequestor)
	{
		return buildGroup("Backend", layout ->
		{
			TextField backendHost = TinyComponents.newTextField();
			map(String.class, backendHost, PropertyKeys.BACKEND_HOST_URL_KEY, () -> "http://localhost:22333");

			layout.addComponent(VaadinUIUtil.labeledFill("Backend URL: ", backendHost));

			layout.addComponent(TinyComponents.newButton("Test", clickEvent -> {
				try
				{
					backendRequestor.runRequest(backendHost.getValue(), null, "/test", Map.of(), new TypeReference<Map<String, String>>()
					{
					});

					new Notification("Test", "Success", Notification.Type.HUMANIZED_MESSAGE).show(Page.getCurrent());
				}
				catch(Exception e)
				{
					new Notification("Test", "Failed", Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
				}
			}));
		});
	}

	private Component buildCaptchaGroup()
	{
		return buildGroup("Captcha", layout ->
		{
			CheckBox enabledCaptcha = TinyComponents.newCheckBox("Enable captcha?");
			map(Boolean.class, enabledCaptcha, PropertyKeys.CAPTCHA_ENABLED_KEY, () -> false);
			layout.addComponent(enabledCaptcha);

			TextField privateApiKey = TinyComponents.newTextField();
			map(String.class, privateApiKey, PropertyKeys.CAPTCHA_PRIVATE_KEY, null);
			layout.addComponent(VaadinUIUtil.labeledFill("Private captcha key: ", privateApiKey));

			TextField siteApiKey = TinyComponents.newTextField();
			map(String.class, siteApiKey, PropertyKeys.CAPTCHA_SITE_KEY, null);
			layout.addComponent(VaadinUIUtil.labeledFill("Site captcha key: ", siteApiKey));

			privateApiKey.setEnabled(enabledCaptcha.getValue());
			siteApiKey.setEnabled(enabledCaptcha.getValue());

			enabledCaptcha.addValueChangeListener(event ->
			{
				privateApiKey.setEnabled(event.getValue());
				siteApiKey.setEnabled(event.getValue());
			});
		});
	}

	private Component buildGithubGroup()
	{
		return buildGroup("Github", layout ->
		{
			TextField oauthKeyField = TinyComponents.newTextField();
			map(String.class, oauthKeyField, GithubPropertyKeys.OAUTH_KEY, null);
			layout.addComponent(VaadinUIUtil.labeledFill("OAuth Key: ", oauthKeyField));

			TextField secretHookKeyField = TinyComponents.newTextField();
			map(String.class, secretHookKeyField, GithubPropertyKeys.SECRET_HOOK_KEY, null);
			layout.addComponent(VaadinUIUtil.labeledFill("Secret Hook Key: ", secretHookKeyField));
		});
	}

	private Component buildGAGroup()
	{
		return buildGroup("Google Analytics", layout ->
		{
			TextField trackerIdField = TinyComponents.newTextField();
			map(String.class, trackerIdField, GAPropertyKeys.TRACKER_ID, null);
			layout.addComponent(VaadinUIUtil.labeledFill("Tracker Id: ", trackerIdField));

			TextField domainNameField = TinyComponents.newTextField();
			map(String.class, domainNameField, GAPropertyKeys.DOMAIN_NAME, null);
			layout.addComponent(VaadinUIUtil.labeledFill("Domain Name: ", domainNameField));
		});
	}

	public static Panel buildGroup(String title, Consumer<VerticalLayout> consumer)
	{
		Panel panel = new Panel(title);
		panel.setSizeFull();

		VerticalLayout verticalLayout = VaadinUIUtil.newVerticalLayout();
		verticalLayout.setMargin(true);
		verticalLayout.setSpacing(true);
		verticalLayout.setSizeFull();
		verticalLayout.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);

		panel.setContent(verticalLayout);

		consumer.accept(verticalLayout);
		return panel;
	}
}
