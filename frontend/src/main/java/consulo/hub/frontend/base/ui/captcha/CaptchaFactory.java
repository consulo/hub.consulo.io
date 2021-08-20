package consulo.hub.frontend.base.ui.captcha;

import com.vaadin.server.Sizeable;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.wcs.wcslib.vaadin.widget.recaptcha.ReCaptcha;
import com.wcs.wcslib.vaadin.widget.recaptcha.shared.ReCaptchaOptions;
import consulo.hub.frontend.UserConfigurationService;
import consulo.hub.frontend.base.ui.util.VaadinUIUtil;
import consulo.hub.frontend.util.PropertyKeys;
import consulo.hub.shared.util.PropertySet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author VISTALL
 * @since 27-Sep-16
 */
@Service
public class CaptchaFactory
{
	private UserConfigurationService myConfigurationService;

	@Autowired
	public CaptchaFactory(UserConfigurationService configurationService)
	{
		myConfigurationService = configurationService;
	}

	public Captcha create()
	{
		PropertySet propertySet = myConfigurationService.getPropertySet();

		boolean enabled = propertySet.getBoolProperty(PropertyKeys.CAPTCHA_ENABLED_KEY);
		if(enabled)
		{
			ReCaptchaOptions reCaptchaOptions = new ReCaptchaOptions();
			reCaptchaOptions.type = "image";
			reCaptchaOptions.theme = "light";
			reCaptchaOptions.sitekey = propertySet.getStringProperty(PropertyKeys.CAPTCHA_SITE_KEY);

			ReCaptcha reCaptcha = new ReCaptcha(propertySet.getStringProperty(PropertyKeys.CAPTCHA_PRIVATE_KEY), reCaptchaOptions);

			return new Captcha()
			{
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
		else
		{
			return new Captcha()
			{
				@Override
				public Component getComponent()
				{
					HorizontalLayout layout = VaadinUIUtil.newHorizontalLayout();
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
	}

}
