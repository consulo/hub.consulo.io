package consulo.webService.auth;

import org.springframework.stereotype.Component;
import com.intellij.util.ArrayUtil;
import com.vaadin.spring.access.ViewAccessControl;
import com.vaadin.ui.UI;
import consulo.webService.auth.view.AdminUserView;
import consulo.webService.errorReporter.view.AdminErrorReportsView;
import consulo.webService.plugins.view.AdminRepositoryView;
import consulo.webService.plugins.view.RepositoryView;

/**
 * This demonstrates how you can control access to views.
 */
@Component
public class VaadinViewAccessControl implements ViewAccessControl
{
	private static final String[] ourWantAdminRole = new String[]{
			makeBeanName(AdminErrorReportsView.ID),
			makeBeanName(AdminRepositoryView.ID),
			makeBeanName(AdminUserView.ID)
	};

	@Override
	public boolean isAccessGranted(UI ui, String beanName)
	{
		if(ArrayUtil.contains(beanName, ourWantAdminRole))
		{
			return SecurityUtil.hasRole(Roles.ROLE_ADMIN);
		}

		if(beanName.startsWith(RepositoryView.ID))
		{
			return true;
		}
		return SecurityUtil.hasRole(Roles.ROLE_USER);
	}

	private static String makeBeanName(String id)
	{
		return id + "View";
	}
}
