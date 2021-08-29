package consulo.hub.frontend.errorReporter.view;

import com.intellij.openapi.util.Couple;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ReflectionUtil;
import com.vaadin.data.HasValue;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import consulo.hub.frontend.backend.service.BackendErrorReporterService;
import consulo.hub.frontend.base.ui.util.TinyComponents;
import consulo.hub.frontend.base.ui.util.VaadinUIUtil;
import consulo.hub.frontend.errorReporter.ui.ScrollableListPanel;
import consulo.hub.shared.errorReporter.domain.ErrorReport;
import consulo.hub.shared.errorReporter.domain.ErrorReportStatus;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 02-Nov-16
 */
public abstract class BaseErrorReportsView extends VerticalLayout implements View
{
	private static final int ourPageSize = 50;

	@Autowired
	protected BackendErrorReporterService myErrorReportRepository;

	protected final Set<ErrorReportStatus> myFilters = new HashSet<>();
	private int myPage = 0;
	protected ScrollableListPanel myReportList;
	protected Label myLabel;
	protected int myLastPageSize;

	public BaseErrorReportsView()
	{
		setMargin(false);
		setSpacing(false);
		setSizeFull();
	}

	protected abstract Page<ErrorReport> getReports(int page, ErrorReportStatus[] errorReportStatuses, int pageSize);

	@Override
	public void enter(ViewChangeListener.ViewChangeEvent event)
	{
		removeAllComponents();

		HorizontalLayout header = VaadinUIUtil.newHorizontalLayout();
		header.addStyleName("headerMargin");
		header.setWidth(100, Unit.PERCENTAGE);
		myLabel = new Label();

		header.addComponent(myLabel);

		myReportList = new ScrollableListPanel();
		myReportList.addStyleName("bodyMargin");

		if(allowFilters())
		{
			HorizontalLayout filters = VaadinUIUtil.newHorizontalLayout();
			filters.setHeight(100, Unit.PERCENTAGE);
			filters.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
			filters.setSpacing(true);
			filters.addComponent(TinyComponents.newLabel("Status: "));

			for(ErrorReportStatus status : ErrorReportStatus.values())
			{
				CheckBox filterBox = TinyComponents.newCheckBox(StringUtils.capitalize(status.name().toLowerCase(Locale.US)));
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
				layout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
				layout.setHeight(2, Unit.EM);
				layout.addStyleName("errorViewLineLayoutBox");
				layout.addStyleName("errorViewLineLayout" + StringUtils.capitalize(status.name().toLowerCase(Locale.US)));

				layout.addComponent(filterBox);

				filters.addComponent(layout);
			}
			header.addComponent(filters);
		}

		addComponent(header);

		addComponent(myReportList);

		setExpandRatio(myReportList, 1f);

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
		myReportList.removeAll();

		Page<ErrorReport> page = getReports(myPage, myFilters.toArray(new ErrorReportStatus[myFilters.size()]), ourPageSize);

		myLastPageSize = page.getNumberOfElements();

		updateHeader();

		for(ErrorReport errorReport : page)
		{
			VerticalLayout lineLayout = VaadinUIUtil.newVerticalLayout();
			lineLayout.setWidth(100, Unit.PERCENTAGE);

			HorizontalLayout shortLine = VaadinUIUtil.newHorizontalLayout();
			lineLayout.addComponent(shortLine);
			lineLayout.addStyleName("errorViewLineLayout");

			shortLine.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
			shortLine.setWidth(100, Unit.PERCENTAGE);

			HorizontalLayout leftLayout = VaadinUIUtil.newHorizontalLayout();
			leftLayout.setWidth(100, Unit.PERCENTAGE);
			leftLayout.setSpacing(true);
			leftLayout.addComponent(TinyComponents.newLabel("Message: " + errorReport.getMessage()));
			leftLayout.addComponent(TinyComponents.newLabel("At: " + new Date(errorReport.getCreateDate())));

			HorizontalLayout rightLayout = VaadinUIUtil.newHorizontalLayout();
			rightLayout.setSpacing(true);

			List<Consumer<ErrorReport>> onUpdate = new ArrayList<>();

			onUpdate.add(report ->
			{
				for(ErrorReportStatus status : ErrorReportStatus.values())
				{
					lineLayout.removeStyleName("errorViewLineLayout" + StringUtils.capitalize(status.name().toLowerCase(Locale.US)));
				}

				lineLayout.addStyleName("errorViewLineLayout" + StringUtils.capitalize(errorReport.getStatus().name().toLowerCase(Locale.US)));
			});
			addRightButtons(errorReport, lineLayout, rightLayout, onUpdate);

			fireChanged(onUpdate, errorReport);

			shortLine.addComponent(leftLayout);
			shortLine.setComponentAlignment(leftLayout, Alignment.MIDDLE_LEFT);

			shortLine.addComponent(rightLayout);
			shortLine.setComponentAlignment(rightLayout, Alignment.MIDDLE_RIGHT);

			myReportList.add(lineLayout);
		}

		if(page.hasPrevious() || page.hasNext())
		{
			HorizontalLayout pageLayout = VaadinUIUtil.newHorizontalLayout();
			pageLayout.setMargin(true);
			pageLayout.setSpacing(true);
			if(page.hasPrevious())
			{
				pageLayout.addComponent(TinyComponents.newButton("Prev", event ->
				{
					myPage--;
					rebuildList();
				}));
			}
			if(page.hasNext())
			{
				pageLayout.addComponent(TinyComponents.newButton("Next", event ->
				{
					myPage++;
					rebuildList();
				}));
			}
			myReportList.add(pageLayout, Alignment.MIDDLE_CENTER);
		}
	}

	protected void updateHeader()
	{
		myLabel.setValue(String.format("Error Reports (%d, page: %d)", myLastPageSize, myPage));
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
		copyLink.setIcon(FontAwesome.CHAIN);

		copyLink.addClickListener(e -> getUI().getPage().open("/errorReport#" + errorReport.getId(), "Error Report"));

		Button detailsButton = TinyComponents.newButton("Details");
		detailsButton.setIcon(FontAwesome.LIST);

		detailsButton.addClickListener(e -> openOrCloseDetails(errorReport, lineLayout, onUpdate));

		rightLayout.addComponent(copyLink);
		rightLayout.addComponent(detailsButton);
	}

	protected void openOrCloseDetails(ErrorReport errorReport, VerticalLayout lineLayout, List<Consumer<ErrorReport>> onUpdate)
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
				if(skipField(field.getName()))
				{
					continue;
				}

				field.setAccessible(true);
				Class<?> type = field.getType();
				if(type == String.class || type == Boolean.class || type == Integer.class || type == Long.class || type.isEnum())
				{
					rows.add(Couple.of(field.getName(), field.getName()));
				}
			}

			GridLayout layout = new GridLayout(2, rows.size());
			layout.setMargin(false);
			layout.setSpacing(false);
			layout.setColumnExpandRatio(0, 0.2f);
			layout.setColumnExpandRatio(1, 0.7f);
			layout.setWidth(100, Unit.PERCENTAGE);

			lineLayout.addComponent(layout);

			fill(layout, errorReport, rows, onUpdate);
		}
	}

	protected boolean skipField(String name)
	{
		return false;
	}

	private static void fill(GridLayout gridLayout, ErrorReport errorReport, List<Couple<String>> list, List<Consumer<ErrorReport>> onUpdate)
	{
		int row = 0;

		for(Couple<String> couple : list)
		{
			gridLayout.addComponent(TinyComponents.newLabel(couple.getFirst() + ":"), 0, row);

			Object rawValue = ReflectionUtil.getField(ErrorReport.class, errorReport, null, couple.getSecond());

			String value = rawValue == null ? null : String.valueOf(rawValue);

			final HasValue<String> textField;
			if(value != null && StringUtil.containsLineBreak(value))
			{
				textField = new TextArea();
				((Component) textField).setHeight(15, Unit.EM);
				((Component) textField).addStyleName(ValoTheme.TEXTAREA_SMALL);
			}
			else
			{
				textField = new TextField();
				((Component) textField).addStyleName(ValoTheme.TEXTFIELD_SMALL);
				((Component) textField).addStyleName(ValoTheme.TEXTFIELD_BORDERLESS);
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
