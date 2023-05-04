package consulo.procoeton.core.vaadin.captcha;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import consulo.procoeton.core.ProPropertiesService;
import consulo.procoeton.core.util.PropertySet;
import consulo.procoeton.core.vaadin.ui.util.VaadinUIUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author VISTALL
 * @since 27-Sep-16
 */
@Service
public class CaptchaFactory
{
	private ProPropertiesService myConfigurationService;

	@Autowired
	public CaptchaFactory(ProPropertiesService configurationService)
	{
		myConfigurationService = configurationService;
	}

	public Captcha create()
	{
		PropertySet propertySet = myConfigurationService.getPropertySet();

		boolean enabled = propertySet.getBoolProperty(CaptchaKeys.CAPTCHA_ENABLED_KEY);
		// TODO [VISTALL] not supported
//		if(enabled)
//		{
//			ReCaptchaOptions reCaptchaOptions = new ReCaptchaOptions();
//			reCaptchaOptions.type = "image";
//			reCaptchaOptions.theme = "light";
//			reCaptchaOptions.sitekey = propertySet.getStringProperty(PropertyKeys.CAPTCHA_SITE_KEY);
//
//			ReCaptcha reCaptcha = new ReCaptcha(propertySet.getStringProperty(PropertyKeys.CAPTCHA_PRIVATE_KEY), reCaptchaOptions);
//
//			return new Captcha()
//			{
//				@Override
//				public Component getComponent()
//				{
//					return reCaptcha;
//				}
//
//				@Override
//				public boolean isValid()
//				{
//					return reCaptcha.validate();
//				}
//
//				@Override
//				public void refresh()
//				{
//					if(!reCaptcha.isEmpty())
//					{
//						reCaptcha.reload();
//					}
//				}
//			};
//		}
//		else
		{
			return new Captcha()
			{
				private final Checkbox captchaBox = new Checkbox("Captcha");

				@Override
				public Component getComponent()
				{
					HorizontalLayout layout = VaadinUIUtil.newHorizontalLayout(captchaBox);
					layout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
					layout.setHeight(30, Unit.PIXELS);
					layout.setWidth(100, Unit.PERCENTAGE);
					return layout;
				}

				@Override
				public boolean isValid()
				{
					return captchaBox.getValue();
				}

				@Override
				public void refresh()
				{
				}
			};
		}
	}

}
