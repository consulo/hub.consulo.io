package consulo.hub.frontend.vflow.login;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.Autocomplete;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.data.validator.StringLengthValidator;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility;
import consulo.hub.frontend.vflow.backend.BackendAuthenticationProvider;
import consulo.hub.frontend.vflow.base.MainLayout;
import consulo.hub.frontend.vflow.base.VChildLayout;
import consulo.hub.frontend.vflow.base.captcha.CaptchaFactory;
import consulo.hub.frontend.vflow.dash.ui.DashboardView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;

/**
 * @author VISTALL
 * @since 30/04/2023
 */
@PageTitle("Login")
@Route(value = "login", layout = MainLayout.class)
@RouteAlias(value = "logout", layout = MainLayout.class)
@AnonymousAllowed
public class LoginView extends VChildLayout implements BeforeEnterObserver
{
	@Autowired
	public LoginView(CaptchaFactory captchaFactory, BackendAuthenticationProvider authenticationProvider)
	{
		VerticalLayout layout = new VerticalLayout();
		layout.setPadding(false);
		layout.setMargin(false);
		layout.setWidth("var(--_vaadin-app-layout-drawer-offset-size)");
		layout.addClassName(LumoUtility.Margin.AUTO);
		add(layout);
		setJustifyContentMode(JustifyContentMode.CENTER);
		setHorizontalComponentAlignment(Alignment.CENTER, layout);

		layout.add(new H2("Login"));

		TextField emailField = new TextField("Email");
		emailField.setWidthFull();
		emailField.setAutocomplete(Autocomplete.USERNAME);
		layout.add(emailField);
		PasswordField passwordField = new PasswordField("Password");
		passwordField.setWidthFull();
		passwordField.setAutocomplete(Autocomplete.CURRENT_PASSWORD);
		layout.add(passwordField);

		consulo.hub.frontend.vflow.base.captcha.Captcha captcha = captchaFactory.create();

		layout.add(captcha.getComponent());

		Binder<AuthRequest> binder = new Binder<>();
		binder.forField(emailField)
				.withValidator(new EmailValidator("Wrong email"))
				.asRequired()
				.bind(AuthRequest::getEmail, AuthRequest::setEmail);
		binder.forField(passwordField)
				.withValidator(new StringLengthValidator("Wrong password", 4, 48))
				.asRequired()
				.bind(AuthRequest::getPassword, AuthRequest::setPassword);

		Button registerButton = new Button("Log in", event -> {
			try
			{
				AuthRequest request = new AuthRequest();
				binder.writeBean(request);

				if(!captcha.isValid())
				{
					Notification notification = new Notification("Captcha failed", 10_000, Notification.Position.TOP_CENTER);
					notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
					notification.open();
					return;
				}

				Authentication authenticate = authenticationProvider.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

				SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();

				SecurityContext newContext = securityContextHolderStrategy.createEmptyContext();
				newContext.setAuthentication(authenticate);
				securityContextHolderStrategy.setContext(newContext);

				VaadinServletRequest vaadinRequest = VaadinServletRequest.getCurrent();

				if(vaadinRequest != null)
				{
					vaadinRequest.getSession().setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, newContext);

					HttpServletRequest httpRequest = vaadinRequest.getHttpServletRequest();
					if(httpRequest != null)
					{
						HttpSession session = vaadinRequest.getSession(false);
						if(session != null)
						{
							httpRequest.changeSessionId();

							session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
						}

						RequestAttributeSecurityContextRepository repository = new RequestAttributeSecurityContextRepository();

						repository.saveContext(newContext, vaadinRequest, null);
					}
				}

				getUI().ifPresent(ui -> ui.navigate(DashboardView.class));
			}
			catch(BadCredentialsException e)
			{
				Notification notification = new Notification("Invalid user or password", 10_000, Notification.Position.TOP_CENTER);
				notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
				notification.open();
			}
			catch(ValidationException ignored)
			{
			}
		});
		registerButton.setWidthFull();
		registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		layout.add(registerButton);
	}

//	@Override
//	protected void onAttach(AttachEvent attachEvent)
//	{
//		getUI().ifPresent(ui -> ui.getPage().executeJs("return window.matchMedia('(prefers-color-scheme: dark)').matches;").then(Boolean.class, isDark -> {
//			ui.getElement().getThemeList().add(Lumo.DARK);
//		}));
//	}
}
