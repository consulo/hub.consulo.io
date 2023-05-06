package consulo.hub.frontend.base;

import com.vaadin.flow.server.VaadinRequest;
import consulo.hub.frontend.PropertiesService;
import org.springframework.beans.factory.annotation.Autowired;
import sun.jvm.hotspot.debugger.Page;

/**
 * @author VISTALL
 * @since 09-Nov-16
 */
@Deprecated
public abstract class BaseUI extends UI
{
	@Autowired
	private PropertiesService myConfigurationService;

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
