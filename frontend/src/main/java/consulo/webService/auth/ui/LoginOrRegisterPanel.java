package consulo.webService.auth.ui;

import com.vaadin.data.Validator;
import com.vaadin.data.ValueContext;
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
import consulo.webService.ui.util.VaadinUIUtil;

public class LoginOrRegisterPanel extends VerticalLayout
{
	private static final Validator<String> ourEmailValidator = new EmailValidator("Bad email");
	private static final Validator<String> ourPasswordValidator = new StringLengthValidator("Bad password", 1, Integer.MAX_VALUE);

	private Captcha myCaptcha;
	private TextField myEmailTextField;
	private PasswordField myPasswordField;

	public LoginOrRegisterPanel(CaptchaFactory captchaFactory, LoginOrRegisterCallback loginCallback, LoginOrRegisterCallback registerCallback)
	{
		VerticalLayout mainPanel = VaadinUIUtil.newVerticalLayout();
		mainPanel.setSizeUndefined();
		mainPanel.setSpacing(true);
		mainPanel.setMargin(true);

		addComponent(mainPanel);
		setComponentAlignment(mainPanel, Alignment.MIDDLE_CENTER);

		myEmailTextField = new TextField("Email");
		myEmailTextField.addStyleName(ValoTheme.TEXTAREA_SMALL);
		myEmailTextField.setWidth(100, Unit.PERCENTAGE);
		mainPanel.addComponent(myEmailTextField);

		myPasswordField = new PasswordField("Password");
		myPasswordField.addStyleName(ValoTheme.TEXTAREA_SMALL);
		myPasswordField.setWidth(100, Unit.PERCENTAGE);
		mainPanel.addComponent(myPasswordField);

		myCaptcha = captchaFactory.create();

		Button loginButton = new Button("Login", createListener("Login failed", loginCallback));
		loginButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
		loginButton.addStyleName(ValoTheme.BUTTON_SMALL);
		loginButton.setClickShortcut(ShortcutAction.KeyCode.ENTER);

		Button registerButton = new Button("Register", createListener("Register failed", registerCallback));
		registerButton.addStyleName(ValoTheme.BUTTON_SMALL);
		mainPanel.addComponent(myCaptcha.getComponent());

		HorizontalLayout buttonsPanel = new HorizontalLayout(loginButton, registerButton);
		buttonsPanel.setComponentAlignment(registerButton, Alignment.MIDDLE_RIGHT);
		buttonsPanel.setWidth(100, Unit.PERCENTAGE);
		buttonsPanel.setSpacing(true);

		mainPanel.addComponent(buttonsPanel);
	}

	private Button.ClickListener createListener(String error, LoginOrRegisterCallback callback)
	{
		return event ->
		{
			if(!myCaptcha.isValid())
			{
				Notification notification = new Notification("ReCaptcha is bad", Notification.Type.ERROR_MESSAGE);
				notification.setPosition(Position.TOP_RIGHT);
				notification.setDelayMsec(3000);
				notification.show(Page.getCurrent());
				return;
			}

			myCaptcha.refresh();

			if(ourEmailValidator.apply(myEmailTextField.getValue(), new ValueContext()).isError() || ourPasswordValidator.apply(myPasswordField.getValue(), new ValueContext()).isError())
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
