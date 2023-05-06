package consulo.hub.frontend.auth.view;

import com.fasterxml.jackson.core.type.TypeReference;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import consulo.hub.frontend.PropertiesService;
import consulo.hub.frontend.auth.ui.LoginOrRegisterPanel;
import consulo.hub.frontend.backend.BackendRequestor;
import consulo.hub.frontend.backend.service.BackendUserAccountService;
import consulo.hub.frontend.base.RootUI;
import consulo.hub.frontend.base.ui.captcha.CaptchaFactory;
import consulo.hub.frontend.config.view.ConfigPanel;
import consulo.hub.frontend.util.PropertyKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component // No SpringView annotation because this view can not be navigated to
@UIScope
public class AccessDeniedView extends VerticalLayout implements View
{
	@Autowired
	private CaptchaFactory myCaptchaFactory;

	@Autowired
	private AuthenticationManager myAuthenticationManager;

	@Autowired
	private BackendUserAccountService myBackendUserAccountService;

	@Autowired
	private PropertiesService myPropertiesService;

	@Autowired
	private BackendRequestor myBackendRequestor;

	public AccessDeniedView()
	{
	}

	@Override
	public void enter(ViewChangeListener.ViewChangeEvent event)
	{
		removeAllComponents();

		if(myPropertiesService.isNotInstalled())
		{
			getUI().getPage().setTitle("Hub / Install");

			Label label = new Label("Install");
			label.addStyleName("headerMargin");
			addComponent(label);

			ConfigPanel configPanel = new ConfigPanel(myBackendRequestor, myPropertiesService, "Install", (properties) ->
			{
				try
				{
					Map<String, String> map = myBackendRequestor.runRequest("/install", Map.of(), new TypeReference<Map<String, String>>()
					{
					});

					String token = map.get("token");

					properties.put(PropertyKeys.BACKEND_HOST_OAUTH_KEY, token);

					myPropertiesService.setProperties(properties);

					new Notification("Install", "Successful installed").show(Page.getCurrent());
				}
				catch(Exception e)
				{
					myPropertiesService.resetProperties();
					
					new Notification("Install", "Failed installing").show(Page.getCurrent());
				}

				getUI().getPage().reload();
			});

			configPanel.addStyleName("bodyMargin");
			addComponent(configPanel);
			setExpandRatio(configPanel, .9f);
			return;
		}

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if(authentication != null)
		{
			Label lbl = new Label("You don't have access to this view.");
			lbl.addStyleName(ValoTheme.LABEL_FAILURE);
			lbl.setSizeUndefined();
			lbl.addStyleName("bodyMargin");
			addComponent(lbl);
		}
		else
		{
			LoginOrRegisterPanel panel = new LoginOrRegisterPanel(myCaptchaFactory, this::login, this::register);
			panel.addStyleName("bodyMargin");
			addComponent(panel);
			setExpandRatio(panel, .9f);
		}
	}

	private boolean login(String username, String password)
	{
		try
		{
			RootUI ui = (RootUI) getUI();

			Authentication token = myAuthenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

			SecurityContextHolder.getContext().setAuthentication(token);

			ui.getNavigator().navigateTo(ui.getNavigator().getState());

			ui.logged();

			return true;
		}
		catch(AuthenticationException ex)
		{
			return false;
		}
	}

	private boolean register(String username, String password)
	{
		if(!myBackendUserAccountService.registerUser(username, password))
		{
			return false;
		}
		return login(username, password);
	}
}
