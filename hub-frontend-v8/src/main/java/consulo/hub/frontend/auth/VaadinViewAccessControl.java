package consulo.hub.frontend.auth;

import com.vaadin.spring.access.ViewAccessControl;
import com.vaadin.ui.UI;
import consulo.hub.frontend.auth.view.AdminUserView;
import consulo.hub.shared.auth.Roles;
import consulo.hub.frontend.PropertiesService;
import consulo.hub.frontend.config.view.AdminConfigView;
import consulo.hub.frontend.errorReporter.view.AdminErrorReportsView;
import consulo.hub.frontend.errorReporter.view.ErrorStatisticsView;
import consulo.hub.frontend.repository.view.AdminRepositoryView;
import consulo.hub.frontend.repository.view.RepositoryView;
import consulo.hub.frontend.statistics.view.AdminStatisticsView;
import consulo.hub.shared.auth.SecurityUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This demonstrates how you can control access to views.
 */
@Component
public class VaadinViewAccessControl implements ViewAccessControl
{
	private static final String[] ourWantAdminRole = new String[]{
			makeBeanName(AdminErrorReportsView.ID),
			makeBeanName(AdminStatisticsView.ID),
			makeBeanName(AdminConfigView.ID),
			makeBeanName(AdminRepositoryView.ID),
			makeBeanName(AdminUserView.ID)
	};

	private static final String[] ourAnonymousView = new String[]{
			makeBeanName(ErrorStatisticsView.ID)
	};

	@Autowired
	private PropertiesService myPropertiesService;

	@Override
	public boolean isAccessGranted(UI ui, String beanName)
	{
		if(myPropertiesService.isNotInstalled())
		{
			return false;
		}

		if(ArrayUtils.contains(ourWantAdminRole, beanName))
		{
			return SecurityUtil.hasRole(Roles.ROLE_SUPERUSER);
		}

		if(beanName.startsWith(RepositoryView.ID) || ArrayUtils.contains(ourAnonymousView, beanName))
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
