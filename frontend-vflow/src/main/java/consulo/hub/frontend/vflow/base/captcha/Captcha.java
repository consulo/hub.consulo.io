package consulo.hub.frontend.vflow.base.captcha;

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
