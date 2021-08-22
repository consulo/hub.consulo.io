package consulo.hub.frontend.errorReporter.view;

import com.intellij.openapi.util.text.StringUtil;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import consulo.hub.frontend.backend.service.ErrorReporterService;
import consulo.hub.frontend.base.ui.util.TinyComponents;
import consulo.hub.frontend.base.ui.util.VaadinUIUtil;
import consulo.hub.frontend.util.AuthUtil;
import consulo.hub.shared.errorReporter.domain.ErrorReport;
import consulo.hub.shared.errorReporter.domain.ErrorReporterStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;
import java.util.function.Consumer;

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

		HorizontalLayout panel = VaadinUIUtil.newHorizontalLayout();
		panel.setSpacing(true);
		rightLayout.addComponent(panel);

		Map<ErrorReporterStatus, Button> adminButtons = new LinkedHashMap<>();
		for(ErrorReporterStatus status : ErrorReporterStatus.values())
		{
			String captalizedStatus = StringUtil.capitalize(status.name().toLowerCase(Locale.US));
			Button button = TinyComponents.newButton(captalizedStatus);
			button.addStyleName("errorViewButton" + captalizedStatus);

			adminButtons.put(status, button);

			button.addClickListener(e ->
			{
				if(errorReport.getStatus() != status)
				{
					fireChanged(onUpdate, errorReport);

					ErrorReport updated = myErrorReportRepository.changeStatus(errorReport.getId(), status, AuthUtil.getUserId());
					if(updated != null)
					{
						fireChanged(onUpdate, updated);
					}
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
				myReportList.remove(lineLayout);
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

		return myErrorReportRepository.findByStatusIn(errorReporterStatuses, new PageRequest(page, pageSize, new Sort(Sort.Direction.DESC, ErrorReporterService.CREATE_DATE)));
	}
}
