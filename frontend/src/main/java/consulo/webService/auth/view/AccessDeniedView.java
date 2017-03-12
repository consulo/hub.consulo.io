package consulo.webService.auth.view;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import consulo.webService.ui.DashboardUI;
import consulo.webService.auth.mongo.service.UserService;
import consulo.webService.auth.ui.LoginOrRegisterPanel;
import consulo.webService.ui.components.CaptchaFactory;

@Component // No SpringView annotation because this view can not be navigated to
@UIScope
public class AccessDeniedView extends VerticalLayout implements View
{
	@Autowired
	private CaptchaFactory myCaptchaFactory;

	@Autowired
	private AuthenticationManager myAuthenticationManager;

	@Autowired
	private UserService myUserService;

	public AccessDeniedView()
	{
		setMargin(true);
	}

	@Override
	public void enter(ViewChangeListener.ViewChangeEvent event)
	{
		removeAllComponents();

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if(authentication != null)
		{
			Label lbl = new Label("You don't have access to this view.");
			lbl.addStyleName(ValoTheme.LABEL_FAILURE);
			lbl.setSizeUndefined();
			addComponent(lbl);
		}
		else
		{
			LoginOrRegisterPanel panel = new LoginOrRegisterPanel(myCaptchaFactory, this::login, this::register);
			addComponent(panel);
			setExpandRatio(panel, .9f);
		}
	}

	private boolean login(String username, String password)
	{
		try
		{
			DashboardUI ui = (DashboardUI) getUI();

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
		if(!myUserService.registerUser(username, password))
		{
			return false;
		}
		return login(username, password);
	}
}
