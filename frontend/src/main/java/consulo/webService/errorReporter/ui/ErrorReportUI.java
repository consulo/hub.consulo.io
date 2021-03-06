package consulo.webService.errorReporter.ui;

import com.intellij.openapi.util.text.StringUtil;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import consulo.webService.errorReporter.domain.ErrorReport;
import consulo.webService.errorReporter.mongo.ErrorReportRepository;
import consulo.webService.errorReporter.view.DirectErrorReportsView;
import consulo.webService.ui.BaseUI;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author VISTALL
 * @since 11-Mar-17
 */
@SpringUI(path = "errorReport")
@Widgetset("consulo.webService.WidgetSet")
@Theme("tests-valo-metro")
@StyleSheet("https://fonts.googleapis.com/css?family=Roboto")
public class ErrorReportUI extends BaseUI
{
	@Autowired
	private ErrorReportRepository myErrorReportRepository;

	@Override
	protected void initImpl(VaadinRequest request, Page page)
	{
		String id = getPage().getUriFragment();

		ErrorReport report = myErrorReportRepository.findOne(StringUtil.notNullize(id));
		if(report == null)
		{
			report = new ErrorReport();
		}

		DirectErrorReportsView reportsView = new DirectErrorReportsView(report);
		setContent(reportsView);
		reportsView.enter(null);
	}
}
