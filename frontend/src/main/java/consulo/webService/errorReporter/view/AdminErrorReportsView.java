package consulo.webService.errorReporter.view;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
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
	protected void addRightButtons(Authentication authentication, ErrorReport errorReport, VerticalLayout lineLayout, HorizontalLayout rightLayout, List<Consumer<ErrorReport>> onUpdate)
	{
		HorizontalLayout panel = new HorizontalLayout();
		panel.setSpacing(true);
		rightLayout.addComponent(panel);

		Map<ErrorReporterStatus, Button> adminButtons = new LinkedHashMap<>();
		for(ErrorReporterStatus status : ErrorReporterStatus.values())
		{
			Button button = TidyComponents.newButton(StringUtil.capitalize(status.name().toLowerCase(Locale.US)));
			button.addStyleName("errorViewButton" + StringUtil.capitalize(status.name().toLowerCase(Locale.US)));

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
		});
		super.addRightButtons(authentication, errorReport, lineLayout, rightLayout, onUpdate);
	}

	@Override
	protected Page<ErrorReport> getReports(Authentication authentication, int page, ErrorReporterStatus[] errorReporterStatuses, int pageSize)
	{
		return myErrorReportRepository.findByStatusIn(errorReporterStatuses, new PageRequest(page, pageSize, new Sort(Sort.Direction.DESC, ErrorReportRepository.CREATE_DATE)));
	}
}
