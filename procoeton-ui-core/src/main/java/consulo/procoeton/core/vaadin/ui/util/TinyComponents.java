package consulo.procoeton.core.vaadin.ui.util;

import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;

/**
 * @author VISTALL
 * @since 12-Sep-16
 */
// use components as is
@Deprecated
public class TinyComponents
{
	public static TextField newTextField()
	{
		return newTextField("");
	}

	public static TextField newTextField(String value)
	{
		if(value == null)
		{
			value = "";
		}
		TextField textField = new TextField();
		textField.setValue(value);
		return textField;
	}

	public static TextArea newTextArea()
	{
		return newTextArea("");
	}

	public static TextArea newTextArea(String value)
	{
		if(value == null)
		{
			value = "";
		}
		TextArea textField = new TextArea();
		textField.setValue(value);
		return textField;
	}
}