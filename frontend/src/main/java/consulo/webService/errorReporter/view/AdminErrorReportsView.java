package consulo.webService.errorReporter.view;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import consulo.webService.errorReporter.domain.ErrorReport;
import consulo.webService.errorReporter.domain.ErrorReporterStatus;
import consulo.webService.errorReporter.mongo.ErrorReportRepository;
import consulo.webService.ui.util.TidyComponents;

/**
 * @author VISTALL
 * @since 02-Nov-16
 */
@SpringView(name = AdminErrorReportsView.ID)
public class AdminErrorReportsView extends BaseErrorReportsView
{
	public static final String ID = "adminErrorReports";

	@Override
	protected void addRightButtons(ErrorReport errorReport, VerticalLayout lineLayout, HorizontalLayout rightLayout, List<Consumer<ErrorReport>> onUpdate)
	{
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if(authentication == null)
		{
			super.addRightButtons(errorReport, lineLayout, rightLayout, onUpdate);
			return;
		}

		HorizontalLayout panel = new HorizontalLayout();
		panel.setSpacing(true);
		rightLayout.addComponent(panel);

		Map<ErrorReporterStatus, Button> adminButtons = new LinkedHashMap<>();
		for(ErrorReporterStatus status : ErrorReporterStatus.values())
		{
			String captalizedStatus = StringUtil.capitalize(status.name().toLowerCase(Locale.US));
			Button button = TidyComponents.newButton(captalizedStatus);
			button.addStyleName("errorViewButton" + captalizedStatus);

			adminButtons.put(status, button);

			button.addClickListener(e ->
			{
				if(errorReport.getStatus() != status)
				{
					errorReport.setChangedByEmail(authentication.getName());
					errorReport.setChangeTime(System.currentTimeMillis());
					errorReport.setStatus(status);

					fireChanged(onUpdate, errorReport);
					myErrorReportRepository.save(errorReport);
				}
			});
		}

		onUpdate.add(report ->
		{
			panel.removeAllComponents();

			for(ErrorReporterStatus errorReporterStatus : ErrorReporterStatus.values())
			{
				if(errorReporterStatus == report.getStatus())
				{
					continue;
				}

				Button button = adminButtons.get(errorReporterStatus);
				panel.addComponent(button);
			}

			// hide from view
			if(!myFilters.contains(report.getStatus()))
			{
				myReportList.removeComponent(lineLayout);
				myLastPageSize--;
				updateHeader();
			}
		});
		super.addRightButtons(errorReport, lineLayout, rightLayout, onUpdate);
	}

	@Override
	protected Page<ErrorReport> getReports(int page, ErrorReporterStatus[] errorReporterStatuses, int pageSize)
	{
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if(authentication == null)
		{
			return new PageImpl<>(Collections.emptyList());
		}

		return myErrorReportRepository.findByStatusIn(errorReporterStatuses, new PageRequest(page, pageSize, new Sort(Sort.Direction.DESC, ErrorReportRepository.CREATE_DATE)));
	}
}
