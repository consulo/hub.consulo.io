package consulo.procoeton.core.vaadin.view.login;

import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.data.validator.StringLengthValidator;

/**
 * @author VISTALL
 * @since 05/05/2023
 */
public class AuthValidators
{
	public static Validator<String> newEmailValidator()
	{
		return new EmailValidator("Wrong email");
	}

	public static Validator<String> newPasswordValidator()
	{
		return new StringLengthValidator("Wrong password", 4, 48);
	}
}
