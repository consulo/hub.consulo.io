package consulo.webService.auth;

import com.vaadin.data.validator.EmailValidator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.event.ShortcutAction;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import com.wcs.wcslib.vaadin.widget.recaptcha.ReCaptcha;
import com.wcs.wcslib.vaadin.widget.recaptcha.shared.ReCaptchaOptions;

public class LoginOrRegisterForm extends VerticalLayout
{
	public LoginOrRegisterForm(LoginOrRegisterCallback loginCallback, LoginOrRegisterCallback registerCallback)
	{
		VerticalLayout mainPanel = new VerticalLayout();
		mainPanel.setSizeUndefined();
		mainPanel.setSpacing(true);
		mainPanel.setMargin(true);

		addComponent(mainPanel);
		setComponentAlignment(mainPanel, Alignment.MIDDLE_CENTER);

		TextField email = new TextField("Email");
		email.setWidth(100, Unit.PERCENTAGE);
		email.setImmediate(true);
		email.addValidator(new EmailValidator("Bad email"));
		mainPanel.addComponent(email);

		PasswordField password = new PasswordField("Password");
		password.setWidth(100, Unit.PERCENTAGE);
		password.addValidator(new StringLengthValidator("Bad password", 1, Integer.MAX_VALUE, false));
		mainPanel.addComponent(password);


		ReCaptchaOptions reCaptchaOptions = new ReCaptchaOptions();
		reCaptchaOptions.type = "image";
		reCaptchaOptions.theme = "light";
		reCaptchaOptions.sitekey = "6LeUWtwSAAAAAM8RcPWKcjKjTroM8K7iK0Oikh6l";

		ReCaptcha reCaptcha = new ReCaptcha("6LeUWtwSAAAAAMN_ao4CuJfC8sh1sWeh0rQPftbQ", reCaptchaOptions);

		Button loginButton = new Button("Login", evt -> {
			if(!email.isValid() || !password.isValid())
			{
				return;
			}

			if(!reCaptcha.validate())
			{
				if(!reCaptcha.isEmpty())
				{
					reCaptcha.reload();
				}

				Notification notification = new Notification("ReCaptcha is bad", Notification.Type.ERROR_MESSAGE);
				notification.setPosition(Position.TOP_RIGHT);
				notification.setDelayMsec(3000);
				notification.show(Page.getCurrent());
				return;
			}

			if(!loginCallback.loginAndPasswordEntered(email.getValue(), password.getValue()))
			{
				if(!reCaptcha.isEmpty())
				{
					reCaptcha.reload();
				}

				Notification notification = new Notification("Login failed", Notification.Type.ERROR_MESSAGE);
				notification.setPosition(Position.TOP_RIGHT);
				notification.setDelayMsec(3000);
				notification.show(Page.getCurrent());
			}
		});
		loginButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
		loginButton.setClickShortcut(ShortcutAction.KeyCode.ENTER);

		Button registerButton = new Button("Register", evt -> {
			if(!email.isValid() || !password.isValid())
			{
				return;
			}

			if(!reCaptcha.validate())
			{
				if(!reCaptcha.isEmpty())
				{
					reCaptcha.reload();
				}

				Notification notification = new Notification("ReCaptcha is bad", Notification.Type.ERROR_MESSAGE);
				notification.setPosition(Position.TOP_RIGHT);
				notification.setDelayMsec(3000);
				notification.show(Page.getCurrent());
				return;
			}

			if(!registerCallback.loginAndPasswordEntered(email.getValue(), password.getValue()))
			{
				if(!reCaptcha.isEmpty())
				{
					reCaptcha.reload();
				}

				Notification notification = new Notification("Register failed", Notification.Type.ERROR_MESSAGE);
				notification.setPosition(Position.TOP_RIGHT);
				notification.setDelayMsec(3000);
				notification.show(Page.getCurrent());
			}
		});


		mainPanel.addComponent(reCaptcha);

		HorizontalLayout buttonsPanel = new HorizontalLayout(loginButton, registerButton);
		buttonsPanel.setComponentAlignment(registerButton, Alignment.MIDDLE_RIGHT);
		buttonsPanel.setWidth(100, Unit.PERCENTAGE);
		buttonsPanel.setSpacing(true);

		mainPanel.addComponent(buttonsPanel);
	}

	@FunctionalInterface
	public interface LoginOrRegisterCallback
	{
		boolean loginAndPasswordEntered(String email, String password);
	}
}
