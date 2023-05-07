package consulo.procoeton.core.vaadin.view.login;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.Autocomplete;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import consulo.hub.shared.auth.domain.UserAccount;
import consulo.procoeton.core.auth.backend.BackendUserRegisterTarget;
import consulo.procoeton.core.backend.BackendRequest;
import consulo.procoeton.core.backend.BackendRequestFactory;
import consulo.procoeton.core.vaadin.captcha.Captcha;
import consulo.procoeton.core.vaadin.captcha.CaptchaFactory;
import consulo.procoeton.core.vaadin.util.Notifications;
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
	private final BackendRequestFactory myBackendRequestFactory;

	@Autowired
	public RegistrationView(CaptchaFactory captchaFactory, BackendRequestFactory backendRequestFactory)
	{
		myCaptchaFactory = captchaFactory;
		myBackendRequestFactory = backendRequestFactory;
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
				.withValidator(AuthValidators.newEmailValidator())
				.asRequired()
				.bind(AuthRequest::getEmail, AuthRequest::setEmail);
		binder.forField(passwordField)
				.withValidator(AuthValidators.newPasswordValidator())
				.asRequired()
				.bind(AuthRequest::getPassword, AuthRequest::setPassword);

		Button registerButton = new Button("Register", event -> {
			try
			{
				AuthRequest request = new AuthRequest();
				binder.writeBean(request);

				if(!captcha.isValid())
				{
					Notifications.error("Captcha failed");
					return;
				}

				BackendRequest<UserAccount> newRequest = myBackendRequestFactory.newRequest(BackendUserRegisterTarget.INSTANCE);
				newRequest.parameter("email", request.getEmail());
				newRequest.parameter("password", request.getPassword());

				UserAccount newAccount = newRequest.execute();
				if(newAccount == null)
				{
					Notifications.error("Failed to register user");
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
