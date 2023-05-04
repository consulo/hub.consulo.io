package consulo.hub.frontend.vflow.errorReporter.view;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldBase;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.router.RouteParameters;
import consulo.procoeton.core.vaadin.ui.Badge;
import consulo.procoeton.core.vaadin.ui.util.TinyComponents;
import consulo.procoeton.core.vaadin.ui.util.VaadinUIUtil;
import consulo.hub.shared.errorReporter.domain.ErrorReport;
import consulo.util.lang.StringUtil;
import org.vaadin.stefan.table.Table;
import org.vaadin.stefan.table.TableDataCell;
import org.vaadin.stefan.table.TableRow;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 01/05/2023
 */
public class ErrorReportComponent extends VerticalLayout
{
	private Div myBadgeHolder;

	public ErrorReportComponent(ErrorReport errorReport)
	{
		myBadgeHolder = new Div();

		HorizontalLayout shortLine = VaadinUIUtil.newHorizontalLayout();

		add(shortLine);

		shortLine.add(myBadgeHolder);

		shortLine.setWidth(100, Unit.PERCENTAGE);

		HorizontalLayout leftLayout = VaadinUIUtil.newHorizontalLayout();
		leftLayout.setWidth(100, Unit.PERCENTAGE);
		leftLayout.setSpacing(true);
		leftLayout.add(TinyComponents.newLabel("Message: " + errorReport.getMessage()));
		leftLayout.add(TinyComponents.newLabel("At: " + new Date(errorReport.getCreateDate())));

		HorizontalLayout rightLayout = VaadinUIUtil.newHorizontalLayout();
		rightLayout.setSpacing(true);

		List<Consumer<ErrorReport>> onUpdate = new ArrayList<>();

		onUpdate.add(report ->
		{
			String status = errorReport.getStatus().name().toLowerCase(Locale.US);

			myBadgeHolder.removeAll();
			Badge badge = new Badge(status);
			badge.addClassName("badge-" + status);
			myBadgeHolder.add(badge);
		});
		addRightButtons(errorReport, this, rightLayout, onUpdate);

		fireChanged(onUpdate, errorReport);

		shortLine.add(leftLayout);
		//shortLine.setComponentAlignment(leftLayout, Alignment.MIDDLE_LEFT);

		shortLine.add(rightLayout);
		//shortLine.setComponentAlignment(rightLayout, Alignment.MIDDLE_RIGHT);
	}

	protected void addRightButtons(ErrorReport errorReport, VerticalLayout lineLayout, HorizontalLayout rightLayout, List<Consumer<ErrorReport>> onUpdate)
	{
		Button externalLink = new Button("External Link", new Icon(VaadinIcon.EXTERNAL_LINK));

		externalLink.addClickListener((e) ->
		{
			UI ui = UI.getCurrent();

			String url = RouteConfiguration.forApplicationScope().getUrl(DirectErrorReportsView.class, new RouteParameters(Map.of(DirectErrorReportsView.LONG_ID, errorReport.getLongId())));

			ui.getPage().open(url, "_blank");
		});

		Button detailsButton = TinyComponents.newButton("Details");
		detailsButton.setIcon(FontAwesome.Solid.LIST.create());

		detailsButton.addClickListener(e -> openOrCloseDetails(errorReport, lineLayout, onUpdate));

		rightLayout.add(externalLink);
		rightLayout.add(detailsButton);
	}

	protected void openOrCloseDetails(ErrorReport errorReport, VerticalLayout lineLayout, List<Consumer<ErrorReport>> onUpdate)
	{
		int componentCount = getComponentCount();
		if(componentCount == 2)
		{
			Component component = getComponentAt(1);
			remove(component);
		}
		else
		{
			List<Field> rows = new ArrayList<>();
			for(Field field : ErrorReport.class.getDeclaredFields())
			{
				if(skipField(field.getName()))
				{
					continue;
				}

				field.setAccessible(true);
				Class<?> type = field.getType();
				if(type == String.class || type == Boolean.class || type == Integer.class || type == Long.class || type.isEnum())
				{
					rows.add(field);
				}
			}

			Table layout = new Table();
			layout.setWidth(100, Unit.PERCENTAGE);

			add(layout);

			fill(layout, errorReport, rows, onUpdate);
		}
	}

	protected boolean skipField(String name)
	{
		return false;
	}

	private static void fill(Table table, ErrorReport errorReport, List<Field> list, List<Consumer<ErrorReport>> onUpdate)
	{
		for(Field field : list)
		{
			TableRow tableRow = table.addRow();
			TableDataCell labelRow = tableRow.addDataCell();
			TableDataCell valueRow = tableRow.addDataCell();

			labelRow.add(TinyComponents.newLabel(field.getName() + ":"));

			Object rawValue = null;
			try
			{
				rawValue = field.get(errorReport);
			}
			catch(IllegalAccessException e)
			{
				continue;
			}

			String value = rawValue == null ? null : String.valueOf(rawValue);

			final TextFieldBase textField;
			if(value != null && StringUtil.containsLineBreak(value))
			{
				textField = new TextArea();
				//((Component) textField).setHeight(15, Unit.EM);
				//((Component) textField).addStyleName(ValoTheme.TEXTAREA_SMALL);
			}
			else
			{
				textField = new TextField();
				//((Component) textField).addStyleName(ValoTheme.TEXTFIELD_SMALL);
				//((Component) textField).addStyleName(ValoTheme.TEXTFIELD_BORDERLESS);
			}

			textField.setWidth(100, Unit.PERCENTAGE);
			textField.setValue(StringUtil.notNullize(value));
			textField.setReadOnly(true);

			valueRow.add((Component) textField);

			onUpdate.add(report ->
			{
				Object rawValue2 = null;
				try
				{
					rawValue2 = field.get(report);
				}
				catch(IllegalAccessException ignored)
				{
				}

				String value2 = rawValue2 == null ? null : String.valueOf(rawValue2);

				textField.setReadOnly(false);
				textField.setValue(StringUtil.notNullize(value2));
				textField.setReadOnly(true);
			});
		}
	}

	public static void fireChanged(List<Consumer<ErrorReport>> consumers, ErrorReport report)
	{
		for(Consumer<ErrorReport> consumer : consumers)
		{
			consumer.accept(report);
		}
	}
}
