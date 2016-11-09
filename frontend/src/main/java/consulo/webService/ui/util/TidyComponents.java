package consulo.webService.ui.util;

import org.jetbrains.annotations.Nullable;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.ValoTheme;

/**
 * @author VISTALL
 * @since 12-Sep-16
 */
public class TidyComponents
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

	public static ComboBox newComboBox()
	{
		ComboBox comboBox = new ComboBox();
		comboBox.setStyleName(ValoTheme.COMBOBOX_TINY);
		comboBox.setNullSelectionAllowed(false);
		comboBox.setTextInputAllowed(false);
		return comboBox;
	}

	public static ListSelect newListSelect()
	{
		ListSelect comboBox = new ListSelect();
		comboBox.setNullSelectionAllowed(false);
		return comboBox;
	}

	public static TextField newTextField()
	{
		return newTextField("");
	}

	public static TextField newTextField(@Nullable String value)
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