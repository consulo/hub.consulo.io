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
import consulo.webService.ui.components.Captcha;
import consulo.webService.ui.components.CaptchaFactory;

public class LoginOrRegisterForm extends VerticalLayout
{
	private Captcha myCaptcha;
	private TextField myEmailTextField;
	private PasswordField myPasswordField;

	public LoginOrRegisterForm(CaptchaFactory captchaFactory, LoginOrRegisterCallback loginCallback, LoginOrRegisterCallback registerCallback)
	{
		VerticalLayout mainPanel = new VerticalLayout();
		mainPanel.setSizeUndefined();
		mainPanel.setSpacing(true);
		mainPanel.setMargin(true);

		addComponent(mainPanel);
		setComponentAlignment(mainPanel, Alignment.MIDDLE_CENTER);

		myEmailTextField = new TextField("Email");
		myEmailTextField.setWidth(100, Unit.PERCENTAGE);
		myEmailTextField.setImmediate(true);
		myEmailTextField.addValidator(new EmailValidator("Bad email"));
		//myEmailTextField.setValidationVisible(true);
		mainPanel.addComponent(myEmailTextField);

		myPasswordField = new PasswordField("Password");
		myPasswordField.setWidth(100, Unit.PERCENTAGE);
		//myPasswordField.setValidationVisible(true);
		myPasswordField.addValidator(new StringLengthValidator("Bad password", 1, Integer.MAX_VALUE, false));
		mainPanel.addComponent(myPasswordField);

		myCaptcha = captchaFactory.create();

		Button loginButton = new Button("Login", createListener("Login failed", loginCallback));
		loginButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
		loginButton.setClickShortcut(ShortcutAction.KeyCode.ENTER);

		Button registerButton = new Button("Register", createListener("Register failed", registerCallback));

		mainPanel.addComponent(myCaptcha.getComponent());

		HorizontalLayout buttonsPanel = new HorizontalLayout(loginButton, registerButton);
		buttonsPanel.setComponentAlignment(registerButton, Alignment.MIDDLE_RIGHT);
		buttonsPanel.setWidth(100, Unit.PERCENTAGE);
		buttonsPanel.setSpacing(true);

		mainPanel.addComponent(buttonsPanel);
	}

	private Button.ClickListener createListener(String error, LoginOrRegisterCallback callback)
	{
		return event -> {
			if(!myCaptcha.isValid())
			{
				Notification notification = new Notification("ReCaptcha is bad", Notification.Type.ERROR_MESSAGE);
				notification.setPosition(Position.TOP_RIGHT);
				notification.setDelayMsec(3000);
				notification.show(Page.getCurrent());
				return;
			}

			myCaptcha.refresh();

			if(!myEmailTextField.isValid() || !myPasswordField.isValid())
			{
				Notification notification = new Notification("Email or password is bad", Notification.Type.ERROR_MESSAGE);
				notification.setPosition(Position.TOP_RIGHT);
				notification.setDelayMsec(3000);
				notification.show(Page.getCurrent());
				return;
			}

			if(!callback.loginAndPasswordEntered(myEmailTextField.getValue(), myPasswordField.getValue()))
			{
				Notification notification = new Notification(error, Notification.Type.ERROR_MESSAGE);
				notification.setPosition(Position.TOP_RIGHT);
				notification.setDelayMsec(3000);
				notification.show(Page.getCurrent());
			}
		};
	}

	@FunctionalInterface
	public interface LoginOrRegisterCallback
	{
		boolean loginAndPasswordEntered(String email, String password);
	}
}
