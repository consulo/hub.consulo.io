package consulo.hub.frontend.vflow.errorReporter.view;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import consulo.hub.frontend.vflow.backend.service.BackendErrorReporterService;
import consulo.hub.shared.errorReporter.domain.ErrorReport;
import consulo.hub.shared.errorReporter.domain.ErrorReportStatus;
import consulo.procoeton.core.backend.BackendServiceDownException;
import consulo.procoeton.core.vaadin.ui.ScrollableLayout;
import consulo.procoeton.core.vaadin.ui.ServerOfflineVChildLayout;
import consulo.procoeton.core.vaadin.ui.util.VaadinUIUtil;
import jakarta.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 02-Nov-16
 */
public abstract class BaseErrorReportsView extends ServerOfflineVChildLayout
{
	private static final int ourPageSize = 50;

	@Autowired
	protected BackendErrorReporterService myErrorReportRepository;

	protected final Set<ErrorReportStatus> myFilters = new HashSet<>();
	private int myPage = 0;
	protected ScrollableLayout myReportList;
	protected int myLastPageSize;
	private final MultiSelectComboBox<ErrorReportStatus> myFilterBox;

	public BaseErrorReportsView()
	{
		super(true);

		myFilterBox = new MultiSelectComboBox<>(null, ErrorReportStatus.values());

		myFilterBox.addValueChangeListener(e ->
		{
			Set<ErrorReportStatus> value = e.getValue();

			myFilters.clear();

			myFilters.addAll(value);

			rebuildList();
		});
	}

	@Override
	public Component getHeaderRightComponent()
	{
		return allowFilters() ? myFilterBox : null;
	}

	protected abstract Page<ErrorReport> getReports(int page, ErrorReportStatus[] errorReportStatuses, int pageSize);

	@Override
	protected void buildLayout(Consumer<Component> uiBuilder)
	{
		removeAll();

		myReportList = new ScrollableLayout();

		if(allowFilters())
		{
			myFilterBox.setValue(ErrorReportStatus.UNKNOWN);
		}

		uiBuilder.accept(myReportList);

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

		Page<ErrorReport> page = null;
		try
		{
			page = getReports(myPage, myFilters.toArray(new ErrorReportStatus[myFilters.size()]), ourPageSize);
		}
		catch(BackendServiceDownException e)
		{
			return;
		}

		myLastPageSize = page.getNumberOfElements();

		updateHeader();

		for(ErrorReport errorReport : page)
		{
			ErrorReportComponent lineLayout = createErrorReportComponent(errorReport);
			lineLayout.setWidth(100, Unit.PERCENTAGE);

			myReportList.addItem(lineLayout);
		}

		if(page.hasPrevious() || page.hasNext())
		{
			HorizontalLayout pageLayout = VaadinUIUtil.newHorizontalLayout();
			pageLayout.setMargin(true);
			pageLayout.setSpacing(true);
			if(page.hasPrevious())
			{
				ComponentEventListener<ClickEvent<Button>> listener = event ->
				{
					myPage--;
					rebuildList();
				};
				pageLayout.add(new Button("Prev", listener));
			}
			if(page.hasNext())
			{
				ComponentEventListener<ClickEvent<Button>> listener = event ->
				{
					myPage++;
					rebuildList();
				};
				pageLayout.add(new Button("Next", listener));
			}
			myReportList.addItem(pageLayout);
		}
	}

	@Nonnull
	protected ErrorReportComponent createErrorReportComponent(ErrorReport errorReport)
	{
		return new ErrorReportComponent(errorReport);
	}

	protected void updateHeader()
	{
		//myLabel.setValue(String.format("Error Reports (%d, page: %d)", myLastPageSize, myPage));
	}
}
