package consulo.webService.auth.view;

import java.util.List;
import java.util.function.Consumer;

import org.springframework.security.core.Authentication;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import consulo.webService.errorReporter.domain.ErrorReport;
import consulo.webService.errorReporter.domain.ErrorReporterStatus;

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
				Button fixedButton = new Button("Fix");
				fixedButton.addStyleName(ValoTheme.BUTTON_TINY);
				fixedButton.addStyleName(ValoTheme.BUTTON_FRIENDLY);
				fixedButton.addClickListener(e -> {
					if(errorReport.getStatus() != ErrorReporterStatus.FIXED)
					{
						errorReport.setChangedByEmail(authentication.getName());
						errorReport.setChangeTime(System.currentTimeMillis());
						errorReport.setStatus(ErrorReporterStatus.FIXED);

						fireChanged(onUpdate, errorReport);
						myErrorReportRepository.save(errorReport);
					}
					rightLayout.removeComponent(fixedButton);
				});
				rightLayout.addComponent(fixedButton);
				break;
			case FIXED:
				break;
		}

		super.addRightButtons(authentication, errorReport, lineLayout, rightLayout, onUpdate);
	}

	@Override
	protected List<ErrorReport> getReports(Authentication authentication)
	{
		return myErrorReportRepository.findAll();
	}
}
