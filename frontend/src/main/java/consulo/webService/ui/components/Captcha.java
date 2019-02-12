package consulo.webService.ui.components;

import javax.annotation.Nonnull;
import com.vaadin.ui.Component;

/**
* @author VISTALL
* @since 27-Sep-16
*/
public interface Captcha
{
	@Nonnull
	Component getComponent();

	boolean isValid();

	void refresh();
}
