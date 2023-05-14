package consulo.hub.frontend.vflow.config.view;

import com.fasterxml.jackson.core.type.TypeReference;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import consulo.hub.frontend.vflow.PropertiesServiceImpl;
import consulo.hub.frontend.vflow.util.PropertyKeys;
import consulo.procoeton.core.backend.ApiBackendKeys;
import consulo.procoeton.core.backend.ApiBackendRequestor;
import consulo.procoeton.core.backend.BackendApiUrl;
import consulo.procoeton.core.github.GithubPropertyKeys;
import consulo.procoeton.core.util.GAPropertyKeys;
import consulo.procoeton.core.util.PropertySet;
import consulo.procoeton.core.vaadin.ui.LabeledLayout;
import consulo.procoeton.core.vaadin.ui.util.TinyComponents;
import consulo.procoeton.core.vaadin.ui.util.VaadinUIUtil;
import consulo.procoeton.core.vaadin.util.Notifications;
import org.apache.commons.lang3.StringUtils;

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
	private final PropertiesServiceImpl myConfigurationService;

	public ConfigPanel(ApiBackendRequestor apiBackendRequestor, PropertiesServiceImpl configurationService, String buttonName, Consumer<Properties> action)
	{
		myConfigurationService = configurationService;
		setSpacing(false);
		setMargin(false);

		VerticalLayout layout = VaadinUIUtil.newVerticalLayout();
		layout.setSizeUndefined();
		layout.setSpacing(true);
		layout.setSizeFull();

		layout.add(buildBackedGroup(apiBackendRequestor));
		layout.add(buildCaptchaGroup());
		layout.add(buildGithubGroup());
		layout.add(buildGAGroup());

		Button installButton = new Button(buttonName);
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
		installButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

		addOthers(layout);

		layout.add(installButton);
		//layout.setComponentAlignment(installButton, Alignment.MIDDLE_RIGHT);

		add(layout);
		//setComponentAlignment(layout, Alignment.MIDDLE_CENTER);
	}

	protected void addOthers(VerticalLayout layout)
	{
	}

	@SuppressWarnings("unchecked")
	private <V> void map(Class<V> clazz, HasValue<? extends HasValue.ValueChangeEvent<V>, V> property, String key, Supplier<V> defSupplier)
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
					property.setValue((V) Boolean.valueOf(value));
				}
				else if(clazz == String.class)
				{
					property.setValue((V) value);
				}
				else
				{
					throw new IllegalArgumentException(clazz.getName());
				}
			}
		}
	}

	private Component buildBackedGroup(ApiBackendRequestor apiBackendRequestor)
	{
		return buildGroup("Backend", layout ->
		{
			TextField backendSecureKeyField = new TextField();
			map(String.class, backendSecureKeyField, ApiBackendKeys.BACKEND_SECURE_KEY, () -> "backend-secure-key");

			TextField backendHubPasswordField = new TextField();
			map(String.class, backendHubPasswordField, ApiBackendKeys.BACKEND_HOST_PASSWORD, () -> "backend-secure-key");

			TextField backendHost = new TextField();
			map(String.class, backendHost, ApiBackendKeys.BACKEND_HOST_URL_KEY, () -> "http://localhost:22333");

			layout.add(VaadinUIUtil.labeledFill("Backend Secure Key: ", backendSecureKeyField));
			layout.add(VaadinUIUtil.labeledFill("Backend Password: ", backendHubPasswordField));
			layout.add(VaadinUIUtil.labeledFill("Backend URL: ", backendHost));

			ComponentEventListener<ClickEvent<Button>> listener = clickEvent -> {
				try
				{
					apiBackendRequestor.runRequest(backendHost.getValue(), BackendApiUrl.toPrivate("/test"), Map.of(), new TypeReference<Map<String, String>>()
					{
					});

					Notifications.info("Success");
				}
				catch(Exception e)
				{
					Notifications.error("Failed");
				}
			};
			layout.add(new Button("Test", listener));
		});
	}

	private Component buildCaptchaGroup()
	{
		return buildGroup("Captcha", layout ->
		{
			Checkbox enabledCaptcha = new Checkbox("Enable captcha?");
			map(Boolean.class, enabledCaptcha, PropertyKeys.CAPTCHA_ENABLED_KEY, () -> false);
			layout.add(enabledCaptcha);

			TextField privateApiKey = TinyComponents.newTextField();
			map(String.class, privateApiKey, PropertyKeys.CAPTCHA_PRIVATE_KEY, null);
			layout.add(VaadinUIUtil.labeledFill("Private captcha key: ", privateApiKey));

			TextField siteApiKey = TinyComponents.newTextField();
			map(String.class, siteApiKey, PropertyKeys.CAPTCHA_SITE_KEY, null);
			layout.add(VaadinUIUtil.labeledFill("Site captcha key: ", siteApiKey));

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
			layout.add(VaadinUIUtil.labeledFill("OAuth Key: ", oauthKeyField));

			TextField secretHookKeyField = TinyComponents.newTextField();
			map(String.class, secretHookKeyField, GithubPropertyKeys.SECRET_HOOK_KEY, null);
			layout.add(VaadinUIUtil.labeledFill("Secret Hook Key: ", secretHookKeyField));
		});
	}

	private Component buildGAGroup()
	{
		return buildGroup("Google Analytics", layout ->
		{
			TextField trackerIdField = TinyComponents.newTextField();
			map(String.class, trackerIdField, GAPropertyKeys.TRACKER_ID, null);
			layout.add(VaadinUIUtil.labeledFill("Tracker Id: ", trackerIdField));

			TextField domainNameField = TinyComponents.newTextField();
			map(String.class, domainNameField, GAPropertyKeys.DOMAIN_NAME, null);
			layout.add(VaadinUIUtil.labeledFill("Domain Name: ", domainNameField));
		});
	}

	public static LabeledLayout buildGroup(String title, Consumer<VerticalLayout> consumer)
	{
		VerticalLayout verticalLayout = VaadinUIUtil.newVerticalLayout();
		verticalLayout.setMargin(true);
		verticalLayout.setSpacing(true);
		verticalLayout.setSizeFull();
		//verticalLayout.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);

		consumer.accept(verticalLayout);
		LabeledLayout layout = new LabeledLayout(title, verticalLayout);
		layout.setWidthFull();
		return layout;
	}
}
