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
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility;
import consulo.hub.frontend.vflow.backend.service.BackendUserAccountService;
import consulo.hub.frontend.vflow.base.MainLayout;
import consulo.hub.frontend.vflow.base.VChildLayout;
import consulo.hub.frontend.vflow.base.captcha.CaptchaFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author VISTALL
 * @since 02/05/2023
 */
@PageTitle("Register")
@Route(value = "register", layout = MainLayout.class)
@AnonymousAllowed
public class RegistrationView extends VChildLayout implements BeforeEnterObserver
{
	@Autowired
	public RegistrationView(BackendUserAccountService backendUserAccountService, CaptchaFactory captchaFactory)
	{
		VerticalLayout layout = new VerticalLayout();
		layout.setPadding(false);
		layout.setMargin(false);
		layout.setWidth("var(--_vaadin-app-layout-drawer-offset-size)");
		layout.addClassName(LumoUtility.Margin.AUTO);
		add(layout);
		setJustifyContentMode(JustifyContentMode.CENTER);
		setHorizontalComponentAlignment(Alignment.CENTER, layout);

		layout.add(new H2("Registration"));

		TextField emailField = new TextField("Email");
		emailField.setWidthFull();
		emailField.setAutocomplete(Autocomplete.USERNAME);
		layout.add(emailField);
		PasswordField passwordField = new PasswordField("Password");
		passwordField.setWidthFull();
		passwordField.setAutocomplete(Autocomplete.NEW_PASSWORD);
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

		Button registerButton = new Button("Register", event -> {
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

				boolean b = backendUserAccountService.registerUser(request.getEmail(), request.getPassword());
				if(!b)
				{
					Notification notification = new Notification("Failed to register user", 10_000, Notification.Position.TOP_CENTER);
					notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
					notification.open();
				}
				else
				{
					getUI().ifPresent(ui -> ui.navigate(LoginView.class));
				}
			}
			catch(ValidationException ignored)
			{
			}
		});
		registerButton.setWidthFull();
		registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		layout.add(registerButton);
	}
}
