package consulo.webService.auth.ui;

import org.jetbrains.annotations.NotNull;
import com.vaadin.ui.Component;

/**
* @author VISTALL
* @since 27-Sep-16
*/
public interface Captcha
{
	@NotNull
	Component getComponent();

	boolean isValid();

	void refresh();
}
