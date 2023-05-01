package consulo.hub.frontend.vflow.config.view;

import com.fasterxml.jackson.core.type.TypeReference;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.theme.lumo.LumoUtility;
import consulo.hub.frontend.vflow.PropertiesService;
import consulo.hub.frontend.vflow.backend.BackendRequestor;
import consulo.hub.frontend.vflow.base.LabeledLayout;
import consulo.hub.frontend.vflow.base.util.TinyComponents;
import consulo.hub.frontend.vflow.base.util.VaadinUIUtil;
import consulo.hub.frontend.vflow.util.GAPropertyKeys;
import consulo.hub.frontend.vflow.util.PropertyKeys;
import consulo.hub.frontend.vflow.util.PropertySet;
import consulo.hub.shared.github.GithubPropertyKeys;
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

		layout.add(buildBackedGroup(backendRequestor));
		layout.add(buildCaptchaGroup());
		layout.add(buildGithubGroup());
		layout.add(buildGAGroup());

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

	private Component buildBackedGroup(BackendRequestor backendRequestor)
	{
		return buildGroup("Backend", layout ->
		{
			TextField backendHost = TinyComponents.newTextField();
			map(String.class, backendHost, PropertyKeys.BACKEND_HOST_URL_KEY, () -> "http://localhost:22333");

			layout.add(VaadinUIUtil.labeledFill("Backend URL: ", backendHost));

			layout.add(TinyComponents.newButton("Test", clickEvent -> {
				try
				{
					backendRequestor.runRequest(backendHost.getValue(), null, "/test", Map.of(), new TypeReference<Map<String, String>>()
					{
					});

					new Notification("Success").open();
				}
				catch(Exception e)
				{
					Notification notification = new Notification("Failed");
					notification.addClassName(LumoUtility.Background.ERROR);
					notification.open();
				}
			}));
		});
	}

	private Component buildCaptchaGroup()
	{
		return buildGroup("Captcha", layout ->
		{
			Checkbox enabledCaptcha = TinyComponents.newCheckBox("Enable captcha?");
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
