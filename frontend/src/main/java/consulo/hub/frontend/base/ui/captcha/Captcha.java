package consulo.hub.frontend.base.ui.captcha;

import javax.annotation.Nonnull;
import com.vaadin.ui.Component;

/**
* @author VISTALL
* @since 27-Sep-16
*/
public interface Captcha
{
	Component getComponent();

	boolean isValid();

	void refresh();
}
