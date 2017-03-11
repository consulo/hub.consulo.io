package consulo.webService.errorReporter.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
import com.vaadin.ui.themes.ValoTheme;
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
		switch(errorReport.getStatus())
		{
			case UNKNOWN:
				List<Button> adminButtons = new ArrayList<>(5);
				for(ErrorReporterStatus status : ErrorReporterStatus.values())
				{
					if(status == ErrorReporterStatus.UNKNOWN)
					{
						continue;
					}

					Button button = TidyComponents.newButton(StringUtil.capitalize(status.name().toLowerCase(Locale.US)));
					if(status == ErrorReporterStatus.FIXED)
					{
						button.addStyleName(ValoTheme.BUTTON_FRIENDLY);
					}

					adminButtons.add(button);

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

					rightLayout.addComponent(button);
				}

				onUpdate.add(report ->
				{
					if(report.getStatus() != ErrorReporterStatus.UNKNOWN)
					{
						for(Button adminButton : adminButtons)
						{
							rightLayout.removeComponent(adminButton);
						}
					}
				});
				break;
		}

		super.addRightButtons(authentication, errorReport, lineLayout, rightLayout, onUpdate);
	}

	@Override
	protected Page<ErrorReport> getReports(Authentication authentication, int page, ErrorReporterStatus[] errorReporterStatuses, int pageSize)
	{
		return myErrorReportRepository.findByStatusIn(errorReporterStatuses, new PageRequest(page, pageSize, new Sort(Sort.Direction.DESC, ErrorReportRepository.CREATE_DATE)));
	}
}
