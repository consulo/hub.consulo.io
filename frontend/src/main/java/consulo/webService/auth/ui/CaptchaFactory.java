package consulo.webService.auth.ui;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.wcs.wcslib.vaadin.widget.recaptcha.ReCaptcha;
import com.wcs.wcslib.vaadin.widget.recaptcha.shared.ReCaptchaOptions;

/**
 * @author VISTALL
 * @since 27-Sep-16
 */
@Service
public class CaptchaFactory
{
	private Environment myEnvironment;

	@Autowired
	public CaptchaFactory(Environment environment)
	{
		myEnvironment = environment;
	}

	@NotNull
	public Captcha create()
	{
		Boolean noCaptcha = myEnvironment.getProperty("no.captcha", Boolean.TYPE);
		if(noCaptcha == Boolean.TRUE)
		{
			return new Captcha()
			{
				@NotNull
				@Override
				public Component getComponent()
				{
					HorizontalLayout layout = new HorizontalLayout();
					layout.setHeight(30, Sizeable.Unit.PIXELS);
					layout.setWidth(100, Sizeable.Unit.PERCENTAGE);
					return layout;
				}

				@Override
				public boolean isValid()
				{
					return true;
				}

				@Override
				public void refresh()
				{

				}
			};
		}
		else
		{
			ReCaptchaOptions reCaptchaOptions = new ReCaptchaOptions();
			reCaptchaOptions.type = "image";
			reCaptchaOptions.theme = "light";
			reCaptchaOptions.sitekey = "6LeUWtwSAAAAAM8RcPWKcjKjTroM8K7iK0Oikh6l";

			ReCaptcha reCaptcha = new ReCaptcha("6LeUWtwSAAAAAMN_ao4CuJfC8sh1sWeh0rQPftbQ", reCaptchaOptions);

			return new Captcha()
			{
				@NotNull
				@Override
				public Component getComponent()
				{
					return reCaptcha;
				}

				@Override
				public boolean isValid()
				{
					return reCaptcha.validate();
				}

				@Override
				public void refresh()
				{
					if(!reCaptcha.isEmpty())
					{
						reCaptcha.reload();
					}
				}
			};
		}
	}

}
