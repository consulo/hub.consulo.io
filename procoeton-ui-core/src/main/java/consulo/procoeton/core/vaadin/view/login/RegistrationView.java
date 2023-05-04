package consulo.procoeton.core.vaadin.view.login;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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
import com.vaadin.flow.router.Location;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import consulo.procoeton.core.auth.backend.BackendUserAccountServiceCore;
import consulo.procoeton.core.vaadin.captcha.Captcha;
import consulo.procoeton.core.vaadin.captcha.CaptchaFactory;
import consulo.procoeton.core.vaadin.view.CenteredView;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author VISTALL
 * @since 02/05/2023
 */
@PageTitle("Register")
@Route(value = "register")
@AnonymousAllowed
public class RegistrationView extends CenteredView implements BeforeEnterObserver
{
	private final CaptchaFactory myCaptchaFactory;
	private final BackendUserAccountServiceCore myUserAccountServiceCore;

	@Autowired
	public RegistrationView(CaptchaFactory captchaFactory, BackendUserAccountServiceCore userAccountServiceCore)
	{
		myCaptchaFactory = captchaFactory;
		myUserAccountServiceCore = userAccountServiceCore;
	}

	@Override
	protected String getHeaderText()
	{
		return "Registration";
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
		passwordField.setAutocomplete(Autocomplete.NEW_PASSWORD);
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

				boolean b = myUserAccountServiceCore.registerUser(request.getEmail(), request.getPassword());
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
