package consulo.hub.frontend.vflow.base.util;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.ComboBoxVariant;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextAreaVariant;
import com.vaadin.flow.component.textfield.TextField;

/**
 * @author VISTALL
 * @since 12-Sep-16
 */
public class TinyComponents
{
	public static Label newLabel(String text)
	{
		Label label = new Label(text);
		//label.addStyleName(ValoTheme.LABEL_SMALL);
		return label;
	}

	public static Checkbox newCheckBox(String text)
	{
		Checkbox comboBox = new Checkbox(text);
		//comboBox.setStyleName(ValoTheme.CHECKBOX_SMALL);
		return comboBox;
	}

	public static <T> ComboBox<T> newComboBox()
	{
		ComboBox<T> comboBox = new ComboBox<>();
		comboBox.addThemeVariants(ComboBoxVariant.LUMO_SMALL);
		comboBox.setAllowCustomValue(false);
		return comboBox;
	}

	public static <T> ListBox<T> newListSelect()
	{
		ListBox<T> select = new ListBox<>();
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
		textField.addThemeVariants();
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
		textField.addThemeVariants(TextAreaVariant.LUMO_SMALL);
		return textField;
	}

	public static Button newButton(String text, ComponentEventListener<ClickEvent<Button>> listener)
	{
		Button button = newButton(text);
		button.addClickListener(listener);
		return button;
	}

	public static Button newButton(String text)
	{
		Button button = new Button(text);
		button.addThemeVariants(ButtonVariant.LUMO_SMALL);
		return button;
	}
}