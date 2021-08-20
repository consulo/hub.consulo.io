package consulo.hub.frontend.base.ui.util;

import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

/**
 * @author VISTALL
 * @since 12-Sep-16
 */
public class TinyComponents
{
	public static Label newLabel(String text)
	{
		Label label = new Label(text);
		label.addStyleName(ValoTheme.LABEL_SMALL);
		return label;
	}

	public static CheckBox newCheckBox(String text)
	{
		CheckBox comboBox = new CheckBox(text);
		comboBox.setStyleName(ValoTheme.CHECKBOX_SMALL);
		return comboBox;
	}

	public static <T> ComboBox<T> newComboBox()
	{
		ComboBox<T> comboBox = new ComboBox<>();
		comboBox.setStyleName(ValoTheme.COMBOBOX_TINY);
		comboBox.setTextInputAllowed(false);
		return comboBox;
	}

	public static <T> ListSelect<T> newListSelect()
	{
		ListSelect<T> select = new ListSelect<>();
		return select;
	}

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
		textField.setStyleName(ValoTheme.TEXTFIELD_TINY);
		return textField;
	}

	public static TextArea newTextArea()
	{
		return newTextArea("");
	}

	public static TextArea newTextArea(
			String value)
	{
		if(value == null)
		{
			value = "";
		}
		TextArea textField = new TextArea();
		textField.setValue(value);
		textField.setStyleName(ValoTheme.TEXTAREA_TINY);
		return textField;
	}

	public static Button newButton(String text, Button.ClickListener listener)
	{
		Button button = newButton(text);
		button.addClickListener(listener);
		return button;
	}

	public static Button newButton(String text)
	{
		Button button = new Button(text);
		button.setStyleName(ValoTheme.BUTTON_TINY);
		return button;
	}
}