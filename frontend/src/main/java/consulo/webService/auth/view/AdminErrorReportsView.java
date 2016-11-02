package consulo.webService.auth.view;

import java.util.List;

import org.springframework.security.core.Authentication;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
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
	protected void addRightButtons(ErrorReport errorReport, VerticalLayout lineLayout, HorizontalLayout rightLayout)
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
						errorReport.setStatus(ErrorReporterStatus.FIXED);
						myErrorReportRepository.save(errorReport);
					}

					// close view
					int componentCount = lineLayout.getComponentCount();
					if(componentCount == 2)
					{
						Component component = lineLayout.getComponent(1);
						lineLayout.removeComponent(component);
					}

					rightLayout.removeComponent(fixedButton);
				});
				rightLayout.addComponent(fixedButton);
				break;
			case FIXED:
				break;
		}

		super.addRightButtons(errorReport, lineLayout, rightLayout);
	}

	@Override
	protected List<ErrorReport> getReports(Authentication authentication)
	{
		return myErrorReportRepository.findAll();
	}
}
