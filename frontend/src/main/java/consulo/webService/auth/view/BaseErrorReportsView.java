package consulo.webService.auth.view;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.intellij.openapi.util.Couple;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ReflectionUtil;
import com.vaadin.data.Property;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import consulo.webService.errorReporter.domain.ErrorReport;
import consulo.webService.errorReporter.domain.ErrorReporterStatus;
import consulo.webService.errorReporter.mongo.ErrorReportRepository;

/**
 * @author VISTALL
 * @since 02-Nov-16
 */
public abstract class BaseErrorReportsView extends VerticalLayout implements View
{
	@Autowired
	protected ErrorReportRepository myErrorReportRepository;

	public BaseErrorReportsView()
	{
		setMargin(true);
	}

	protected abstract List<ErrorReport> getReports(Authentication authentication);

	@Override
	public void enter(ViewChangeListener.ViewChangeEvent event)
	{
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if(authentication == null)
		{
			return;
		}

		List<ErrorReport> errorReports = getReports(authentication);

		HorizontalLayout header = new HorizontalLayout();
		header.setWidth(100, Unit.PERCENTAGE);
		header.addComponent(new Label(String.format("Error Reports (%d)", errorReports.size())));
		addComponent(header);

		VerticalLayout list = new VerticalLayout();
		list.setSpacing(true);
		list.setWidth(100, Unit.PERCENTAGE);
		addComponent(list);


		for(ErrorReport errorReport : errorReports)
		{
			VerticalLayout lineLayout = new VerticalLayout();
			lineLayout.addStyleName(ValoTheme.LAYOUT_CARD);
			lineLayout.setWidth(100, Unit.PERCENTAGE);

			HorizontalLayout shortLine = new HorizontalLayout();
			lineLayout.addComponent(shortLine);

			shortLine.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
			shortLine.setWidth(100, Unit.PERCENTAGE);

			shortLine.addComponent(new Label("Message: " + errorReport.getMessage()));
			shortLine.addComponent(new Label("At: " + new Date(errorReport.getCreateDate())));

			HorizontalLayout rightLayout = new HorizontalLayout();
			rightLayout.setSpacing(true);

			addRightButtons(errorReport, lineLayout, rightLayout);

			shortLine.addComponent(rightLayout);
			shortLine.setComponentAlignment(rightLayout, Alignment.MIDDLE_RIGHT);

			list.addComponent(lineLayout);
		}
	}

	protected void addRightButtons(ErrorReport errorReport, VerticalLayout lineLayout, HorizontalLayout rightLayout)
	{
		Button detailsButton = new Button("Details");
		detailsButton.addStyleName(ValoTheme.BUTTON_TINY);
		if(errorReport.getStatus() == ErrorReporterStatus.FIXED)
		{
			detailsButton.addStyleName(ValoTheme.BUTTON_FRIENDLY);
		}
		detailsButton.addClickListener(e -> {
			int componentCount = lineLayout.getComponentCount();
			if(componentCount == 2)
			{
				Component component = lineLayout.getComponent(1);
				lineLayout.removeComponent(component);
			}
			else
			{
				List<Couple<String>> rows = new ArrayList<>();
				for(Field field : ErrorReport.class.getDeclaredFields())
				{
					field.setAccessible(true);
					Class<?> type = field.getType();
					if(type == String.class || type == Boolean.class || type == Integer.class || type.isEnum())
					{
						rows.add(Couple.of(field.getName(), field.getName()));
					}
				}

				GridLayout layout = new GridLayout(2, rows.size());
				layout.setColumnExpandRatio(0, 0.2f);
				layout.setColumnExpandRatio(1, 0.7f);
				layout.setWidth(100, Unit.PERCENTAGE);

				lineLayout.addComponent(layout);

				fill(layout, errorReport, rows);
			}
		});

		rightLayout.addComponent(detailsButton);
	}

	private static void fill(GridLayout gridLayout, ErrorReport errorReport, List<Couple<String>> list)
	{
		int row = 0;

		for(Couple<String> couple : list)
		{
			gridLayout.addComponent(new Label(couple.getFirst() + ":"), 0, row);

			Object rawValue = ReflectionUtil.getField(ErrorReport.class, errorReport, null, couple.getSecond());

			String value = String.valueOf(rawValue);

			Property<String> textField = null;
			if(value != null && StringUtil.containsLineBreak(value))
			{
				textField = new TextArea();
				((Component) textField).setHeight(15, Unit.EM);
			}
			else
			{
				textField = new Label();
			}

			((Component) textField).setWidth(100, Unit.PERCENTAGE);
			textField.setValue(value);
			textField.setReadOnly(true);

			gridLayout.addComponent((Component) textField, 1, row);

			row++;
		}
	}
}
