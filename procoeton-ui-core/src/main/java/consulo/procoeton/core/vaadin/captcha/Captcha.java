package consulo.procoeton.core.vaadin.captcha;

import com.vaadin.flow.component.Component;

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
