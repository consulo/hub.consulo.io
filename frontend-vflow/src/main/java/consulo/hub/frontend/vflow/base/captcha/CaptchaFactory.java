package consulo.hub.frontend.vflow.base.captcha;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import consulo.hub.frontend.vflow.PropertiesService;
import consulo.hub.frontend.vflow.base.util.VaadinUIUtil;
import consulo.hub.frontend.vflow.util.PropertyKeys;
import consulo.hub.frontend.vflow.util.PropertySet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author VISTALL
 * @since 27-Sep-16
 */
@Service
public class CaptchaFactory
{
	private PropertiesService myConfigurationService;

	@Autowired
	public CaptchaFactory(PropertiesService configurationService)
	{
		myConfigurationService = configurationService;
	}

	public Captcha create()
	{
		PropertySet propertySet = myConfigurationService.getPropertySet();

		boolean enabled = propertySet.getBoolProperty(PropertyKeys.CAPTCHA_ENABLED_KEY);
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
				@Override
				public Component getComponent()
				{
					HorizontalLayout layout = VaadinUIUtil.newHorizontalLayout();
					layout.setHeight(30, Unit.PIXELS);
					layout.setWidth(100, Unit.PERCENTAGE);
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