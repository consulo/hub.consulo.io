package consulo.hub.frontend.vflow.errorReporter.view;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldBase;
import com.vaadin.flow.router.AfterNavigationEvent;
import consulo.hub.frontend.vflow.backend.service.BackendErrorReporterService;
import consulo.hub.frontend.vflow.base.VChildLayout;
import consulo.hub.frontend.vflow.base.util.TinyComponents;
import consulo.hub.frontend.vflow.base.util.VaadinUIUtil;
import consulo.hub.frontend.vflow.errorReporter.ui.ScrollableListPanel;
import consulo.hub.shared.errorReporter.domain.ErrorReport;
import consulo.hub.shared.errorReporter.domain.ErrorReportStatus;
import consulo.util.lang.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.vaadin.stefan.table.Table;
import org.vaadin.stefan.table.TableDataCell;
import org.vaadin.stefan.table.TableRow;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 02-Nov-16
 */
public abstract class BaseErrorReportsView extends VChildLayout
{
	private static final int ourPageSize = 50;

	@Autowired
	protected BackendErrorReporterService myErrorReportRepository;

	protected final Set<ErrorReportStatus> myFilters = new HashSet<>();
	private int myPage = 0;
	protected ScrollableListPanel myReportList;
	protected int myLastPageSize;

	public BaseErrorReportsView()
	{
		setMargin(false);
		setSpacing(false);
		setSizeFull();
	}

	protected abstract Page<ErrorReport> getReports(int page, ErrorReportStatus[] errorReportStatuses, int pageSize);

	@Override
	public void viewReady(AfterNavigationEvent afterNavigationEvent)
	{
		removeAll();

		myReportList = new ScrollableListPanel();
		//myReportList.addStyleName("bodyMargin");

		if(allowFilters())
		{
			HorizontalLayout filters = VaadinUIUtil.newHorizontalLayout();
			filters.setHeight(100, Unit.PERCENTAGE);
			//filters.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
			filters.setSpacing(true);
			filters.add(TinyComponents.newLabel("Status: "));

			for(ErrorReportStatus status : ErrorReportStatus.values())
			{
				Checkbox filterBox = TinyComponents.newCheckBox(StringUtils.capitalize(status.name().toLowerCase(Locale.US)));
				if(status == ErrorReportStatus.UNKNOWN)
				{
					filterBox.setValue(true);
				}

				filterBox.addValueChangeListener(e ->
				{
					if(e.getValue())
					{
						myFilters.add(status);
					}
					else
					{
						myFilters.remove(status);
					}

					rebuildList();
				});

				HorizontalLayout layout = VaadinUIUtil.newHorizontalLayout();
				//layout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
				layout.setHeight(2, Unit.EM);
				//layout.addStyleName("errorViewLineLayoutBox");
				//layout.addStyleName("errorViewLineLayout" + StringUtils.capitalize(status.name().toLowerCase(Locale.US)));

				layout.add(filterBox);

				filters.add(layout);
			}
			//header.addComponent(filters);
		}

		//addComponent(header);

		add(myReportList);

		setFlexGrow(1, myReportList);

		if(allowFilters())
		{
			myFilters.add(ErrorReportStatus.UNKNOWN);
		}
		else
		{
			Collections.addAll(myFilters, ErrorReportStatus.values());
		}

		rebuildList();
	}

	protected boolean allowFilters()
	{
		return true;
	}

	private void rebuildList()
	{
		myReportList.removeAllItems();

		Page<ErrorReport> page = getReports(myPage, myFilters.toArray(new ErrorReportStatus[myFilters.size()]), ourPageSize);

		myLastPageSize = page.getNumberOfElements();

		updateHeader();

		for(ErrorReport errorReport : page)
		{
			VerticalLayout lineLayout = VaadinUIUtil.newVerticalLayout();
			lineLayout.setWidth(100, Unit.PERCENTAGE);

			HorizontalLayout shortLine = VaadinUIUtil.newHorizontalLayout();
			lineLayout.add(shortLine);
			//lineLayout.addStyleName("errorViewLineLayout");

			//shortLine.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
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
				for(ErrorReportStatus status : ErrorReportStatus.values())
				{
					//lineLayout.removeStyleName("errorViewLineLayout" + StringUtils.capitalize(status.name().toLowerCase(Locale.US)));
				}

				//lineLayout.addStyleName("errorViewLineLayout" + StringUtils.capitalize(errorReport.getStatus().name().toLowerCase(Locale.US)));
			});
			addRightButtons(errorReport, lineLayout, rightLayout, onUpdate);

			fireChanged(onUpdate, errorReport);

			shortLine.add(leftLayout);
			//shortLine.setComponentAlignment(leftLayout, Alignment.MIDDLE_LEFT);

			shortLine.add(rightLayout);
			//shortLine.setComponentAlignment(rightLayout, Alignment.MIDDLE_RIGHT);

			myReportList.addItem(lineLayout);
		}

		if(page.hasPrevious() || page.hasNext())
		{
			HorizontalLayout pageLayout = VaadinUIUtil.newHorizontalLayout();
			pageLayout.setMargin(true);
			pageLayout.setSpacing(true);
			if(page.hasPrevious())
			{
				pageLayout.add(TinyComponents.newButton("Prev", event ->
				{
					myPage--;
					rebuildList();
				}));
			}
			if(page.hasNext())
			{
				pageLayout.add(TinyComponents.newButton("Next", event ->
				{
					myPage++;
					rebuildList();
				}));
			}
			myReportList.addItem(pageLayout, Alignment.CENTER);
		}
	}

	protected void updateHeader()
	{
		//myLabel.setValue(String.format("Error Reports (%d, page: %d)", myLastPageSize, myPage));
	}

	protected static void fireChanged(List<Consumer<ErrorReport>> consumers, ErrorReport report)
	{
		for(Consumer<ErrorReport> consumer : consumers)
		{
			consumer.accept(report);
		}
	}

	protected void addRightButtons(ErrorReport errorReport, VerticalLayout lineLayout, HorizontalLayout rightLayout, List<Consumer<ErrorReport>> onUpdate)
	{
		Button copyLink = TinyComponents.newButton("External Link");
		copyLink.setIcon(FontAwesome.Solid.CHAIN.create());

		//TODO copyLink.addClickListener(e -> getUI().getPage().open("/errorReport#" + errorReport.getId(), "Error Report"));

		Button detailsButton = TinyComponents.newButton("Details");
		detailsButton.setIcon(FontAwesome.Solid.LIST.create());

		detailsButton.addClickListener(e -> openOrCloseDetails(errorReport, lineLayout, onUpdate));

		rightLayout.add(copyLink);
		rightLayout.add(detailsButton);
	}

	protected void openOrCloseDetails(ErrorReport errorReport, VerticalLayout lineLayout, List<Consumer<ErrorReport>> onUpdate)
	{
		int componentCount = lineLayout.getComponentCount();
		if(componentCount == 2)
		{
			Component component = lineLayout.getComponentAt(1);
			lineLayout.remove(component);
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

			lineLayout.add(layout);

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
}
