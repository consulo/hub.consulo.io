package consulo.webService.ui;

import org.springframework.beans.factory.annotation.Autowired;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;
import consulo.webService.UserConfigurationService;

/**
 * @author VISTALL
 * @since 09-Nov-16
 */
public abstract class BaseUI extends UI
{
	@Autowired
	private UserConfigurationService myConfigurationService;

	@Override
	protected final void init(VaadinRequest request)
	{
		Page page = getPage();
		if(myConfigurationService.isNotInstalled())
		{
			page.setLocation("/");
			return;
		}

		initImpl(request, page);
	}

	protected abstract void initImpl(VaadinRequest request, Page page);
}
