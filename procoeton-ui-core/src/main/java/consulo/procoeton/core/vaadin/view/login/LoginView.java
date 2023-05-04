package consulo.procoeton.core.vaadin.view.login;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Anchor;
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
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility;
import consulo.procoeton.core.auth.backend.BackendAuthenticationProvider;
import consulo.procoeton.core.vaadin.captcha.Captcha;
import consulo.procoeton.core.vaadin.captcha.CaptchaFactory;
import consulo.procoeton.core.vaadin.view.CenteredView;
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
@Route(value = "login")
@RouteAlias(value = "logout")
@AnonymousAllowed
public class LoginView extends CenteredView implements BeforeEnterObserver
{
	private final CaptchaFactory myCaptchaFactory;
	private final BackendAuthenticationProvider myAuthenticationProvider;

	@Autowired
	public LoginView(CaptchaFactory captchaFactory, BackendAuthenticationProvider authenticationProvider)
	{
		myCaptchaFactory = captchaFactory;
		myAuthenticationProvider = authenticationProvider;
	}

	@Override
	protected void fill(VerticalLayout layout, Location location)
	{
		TextField emailField = new TextField("Email");
		emailField.setWidthFull();
		emailField.setAutocomplete(Autocomplete.USERNAME);
		layout.add(emailField);
		PasswordField passwordField = new PasswordField("Password");
		passwordField.setWidthFull();
		passwordField.setAutocomplete(Autocomplete.CURRENT_PASSWORD);
		layout.add(passwordField);

		Captcha captcha = myCaptchaFactory.create();

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

		Button loginButton = new Button("Log in", event ->
		{
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

				Authentication authenticate = myAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

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

				getUI().ifPresent(ui -> ui.navigate("/"));
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
		loginButton.setWidthFull();
		loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		layout.add(loginButton);

		Anchor registeAnchor = new Anchor("/register", "Register");
		registeAnchor.addClassName(LumoUtility.Margin.AUTO);
		layout.add(registeAnchor);
	}

	@Override
	protected String getHeaderText()
	{
		return "Login";
	}

	//	@Override
//	protected void onAttach(AttachEvent attachEvent)
//	{
//		getUI().ifPresent(ui -> ui.getPage().executeJs("return window.matchMedia('(prefers-color-scheme: dark)').matches;").then(Boolean.class, isDark -> {
//			ui.getElement().getThemeList().add(Lumo.DARK);
//		}));
//	}
}
