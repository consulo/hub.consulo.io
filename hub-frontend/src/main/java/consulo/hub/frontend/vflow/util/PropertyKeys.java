package consulo.hub.frontend.vflow.util;

import consulo.procoeton.core.backend.ApiBackendKeys;
import consulo.procoeton.core.vaadin.captcha.CaptchaKeys;

/**
 * @author VISTALL
 * @since 2016-11-09
 */
@Deprecated
public interface PropertyKeys {
    @Deprecated
    String CAPTCHA_ENABLED_KEY = CaptchaKeys.CAPTCHA_ENABLED_KEY;
    @Deprecated
    String CAPTCHA_SITE_KEY = CaptchaKeys.CAPTCHA_SITE_KEY;
    @Deprecated
    String CAPTCHA_PRIVATE_KEY = CaptchaKeys.CAPTCHA_PRIVATE_KEY;

    @Deprecated
    String BACKEND_HOST_URL_KEY = ApiBackendKeys.BACKEND_HOST_URL_KEY;
    @Deprecated
    String BACKEND_HOST_PASSWORD = ApiBackendKeys.BACKEND_HOST_PASSWORD;
}
