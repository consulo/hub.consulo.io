package consulo.hub.frontend.vflow.login;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.Lumo;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author VISTALL
 * @since 30/04/2023
 */
@Route("login")
@RouteAlias("logout")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver
{
	private LoginForm login;

	@Autowired
	public LoginView()
	{
		addClassName("login-view");
		setSizeFull();

		LoginI18n i18n = LoginI18n.createDefault();

		login = new LoginForm(i18n);
		login.setForgotPasswordButtonVisible(false);

		setJustifyContentMode(JustifyContentMode.CENTER);
		setAlignItems(Alignment.CENTER);

		login.setAction("login");

		add(new H1("Test"), login);
	}

	@Override
	protected void onAttach(AttachEvent attachEvent)
	{
		getUI().ifPresent(ui -> ui.getPage().executeJs("return window.matchMedia('(prefers-color-scheme: dark)').matches;").then(Boolean.class, isDark -> {
			ui.getElement().getThemeList().add(Lumo.DARK);
		}));
	}

	@Override
	public void beforeEnter(BeforeEnterEvent beforeEnterEvent)
	{
		if(beforeEnterEvent.getLocation()
				.getQueryParameters()
				.getParameters()
				.containsKey("error"))
		{
			login.setError(true);
		}
	}
}
