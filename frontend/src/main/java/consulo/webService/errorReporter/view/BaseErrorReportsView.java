package consulo.webService.errorReporter.view;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
import com.vaadin.ui.CheckBox;
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
import consulo.webService.ui.util.TidyComponents;

/**
 * @author VISTALL
 * @since 02-Nov-16
 */
public abstract class BaseErrorReportsView extends VerticalLayout implements View
{
	private static final int ourPageSize = 50;

	@Autowired
	protected ErrorReportRepository myErrorReportRepository;

	private final Set<ErrorReporterStatus> myFilters = new HashSet<>();
	private int myPage = 0;

	public BaseErrorReportsView()
	{
		setMargin(true);
	}

	protected abstract Page<ErrorReport> getReports(Authentication authentication, int page, ErrorReporterStatus[] errorReporterStatuses, int ourPageSize);

	@Override
	public void enter(ViewChangeListener.ViewChangeEvent event)
	{
		HorizontalLayout header = new HorizontalLayout();
		header.setWidth(100, Unit.PERCENTAGE);
		Label label = new Label();

		header.addComponent(label);

		HorizontalLayout filters = new HorizontalLayout();
		filters.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
		filters.setSpacing(true);
		filters.addComponent(TidyComponents.newLabel("Status: "));

		VerticalLayout reportList = new VerticalLayout();
		reportList.setWidth(100, Unit.PERCENTAGE);

		for(ErrorReporterStatus status : ErrorReporterStatus.values())
		{
			CheckBox filterBox = TidyComponents.newCheckBox(StringUtil.capitalize(status.name().toLowerCase(Locale.US)));
			if(status == ErrorReporterStatus.UNKNOWN)
			{
				filterBox.setValue(true);
			}

			filterBox.addValueChangeListener(e ->
			{
				if((Boolean) e.getProperty().getValue())
				{
					myFilters.add(status);
				}
				else
				{
					myFilters.remove(status);
				}
				build(label, reportList);
			});

			filters.addComponent(filterBox);
		}

		header.addComponent(filters);

		addComponent(header);

		addComponent(reportList);

		myFilters.add(ErrorReporterStatus.UNKNOWN);

		build(label, reportList);
	}

	private void build(Label label, VerticalLayout reportList)
	{
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if(authentication == null)
		{
			return;
		}

		reportList.removeAllComponents();

		Page<ErrorReport> page = getReports(authentication, myPage, myFilters.toArray(new ErrorReporterStatus[myFilters.size()]), ourPageSize);

		label.setValue(String.format("Error Reports (%d, page: %d)", page.getNumberOfElements(), myPage));

		boolean step = true;
		for(ErrorReport errorReport : page)
		{
			VerticalLayout lineLayout = new VerticalLayout();
			lineLayout.setWidth(100, Unit.PERCENTAGE);

			HorizontalLayout shortLine = new HorizontalLayout();
			lineLayout.addComponent(shortLine);
			lineLayout.addStyleName(step ? "errorViewLineLayout" : "errorViewLineLayout2");

			step = !step;

			shortLine.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
			shortLine.setWidth(100, Unit.PERCENTAGE);

			shortLine.addComponent(TidyComponents.newLabel("Message: " + errorReport.getMessage()));
			shortLine.addComponent(TidyComponents.newLabel("At: " + new Date(errorReport.getCreateDate())));

			HorizontalLayout rightLayout = new HorizontalLayout();
			rightLayout.setSpacing(true);

			List<Consumer<ErrorReport>> onUpdate = new ArrayList<>();

			addRightButtons(authentication, errorReport, lineLayout, rightLayout, onUpdate);

			fireChanged(onUpdate, errorReport);

			shortLine.addComponent(rightLayout);
			shortLine.setComponentAlignment(rightLayout, Alignment.MIDDLE_RIGHT);

			reportList.addComponent(lineLayout);
		}

		if(page.hasPrevious() || page.hasNext())
		{
			HorizontalLayout pageLayout = new HorizontalLayout();
			pageLayout.setMargin(true);
			pageLayout.setSpacing(true);
			if(page.hasPrevious())
			{
				pageLayout.addComponent(TidyComponents.newButton("Prev", event ->
				{
					myPage--;
					build(label, reportList);
				}));
			}
			if(page.hasNext())
			{
				pageLayout.addComponent(TidyComponents.newButton("Next", event ->
				{
					myPage++;
					build(label, reportList);
				}));
			}
			reportList.addComponent(pageLayout);
			reportList.setComponentAlignment(pageLayout, Alignment.MIDDLE_CENTER);
		}
	}

	protected static void fireChanged(List<Consumer<ErrorReport>> consumers, ErrorReport report)
	{
		for(Consumer<ErrorReport> consumer : consumers)
		{
			consumer.accept(report);
		}
	}

	protected void addRightButtons(Authentication authentication, ErrorReport errorReport, VerticalLayout lineLayout, HorizontalLayout rightLayout, List<Consumer<ErrorReport>> onUpdate)
	{
		Button detailsButton = TidyComponents.newButton("Details");

		onUpdate.add(e ->
		{
			if(e.getStatus() == ErrorReporterStatus.FIXED)
			{
				detailsButton.addStyleName(ValoTheme.BUTTON_FRIENDLY);
			}
		});

		detailsButton.addClickListener(e ->
		{
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
					if(type == String.class || type == Boolean.class || type == Integer.class || type == Long.class || type.isEnum())
					{
						rows.add(Couple.of(field.getName(), field.getName()));
					}
				}

				GridLayout layout = new GridLayout(2, rows.size());
				layout.setColumnExpandRatio(0, 0.2f);
				layout.setColumnExpandRatio(1, 0.7f);
				layout.setWidth(100, Unit.PERCENTAGE);

				lineLayout.addComponent(layout);

				fill(layout, errorReport, rows, onUpdate);
			}
		});

		rightLayout.addComponent(detailsButton);
	}

	private static void fill(GridLayout gridLayout, ErrorReport errorReport, List<Couple<String>> list, List<Consumer<ErrorReport>> onUpdate)
	{
		int row = 0;

		for(Couple<String> couple : list)
		{
			gridLayout.addComponent(TidyComponents.newLabel(couple.getFirst() + ":"), 0, row);

			Object rawValue = ReflectionUtil.getField(ErrorReport.class, errorReport, null, couple.getSecond());

			String value = rawValue == null ? null : String.valueOf(rawValue);

			final Property<String> textField;
			if(value != null && StringUtil.containsLineBreak(value))
			{
				textField = new TextArea();
				((Component) textField).setHeight(15, Unit.EM);
				((Component) textField).addStyleName(ValoTheme.TEXTAREA_SMALL);
			}
			else
			{
				textField = new Label();
				((Component) textField).addStyleName(ValoTheme.LABEL_SMALL);
			}

			((Component) textField).setWidth(100, Unit.PERCENTAGE);
			textField.setValue(StringUtil.notNullize(value));
			textField.setReadOnly(true);

			gridLayout.addComponent((Component) textField, 1, row);

			onUpdate.add(report ->
			{
				Object rawValue2 = ReflectionUtil.getField(ErrorReport.class, report, null, couple.getSecond());

				String value2 = rawValue2 == null ? null : String.valueOf(rawValue2);

				textField.setReadOnly(false);
				textField.setValue(StringUtil.notNullize(value2));
				textField.setReadOnly(true);
			});

			row++;
		}
	}
}
