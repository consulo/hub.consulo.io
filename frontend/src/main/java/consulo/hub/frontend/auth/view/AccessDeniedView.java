package consulo.hub.frontend.auth.view;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import consulo.hub.frontend.backend.service.UserAccountService;
import consulo.hub.frontend.auth.ui.LoginOrRegisterPanel;
import consulo.hub.frontend.PropertiesService;
import consulo.hub.frontend.config.view.ConfigPanel;
import consulo.hub.frontend.base.RootUI;
import consulo.hub.frontend.base.ui.captcha.CaptchaFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component // No SpringView annotation because this view can not be navigated to
@UIScope
public class AccessDeniedView extends VerticalLayout implements View
{
	@Autowired
	private CaptchaFactory myCaptchaFactory;

	@Autowired
	private AuthenticationManager myAuthenticationManager;

	@Autowired
	private UserAccountService myUserAccountService;

	@Autowired
	private PropertiesService myPropertiesService;

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

			ConfigPanel configPanel = new ConfigPanel(myPropertiesService, "Install", () -> getUI().getPage().reload());
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
		if(!myUserAccountService.registerUser(username, password))
		{
			return false;
		}
		return login(username, password);
	}
}
