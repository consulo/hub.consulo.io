package consulo.hub.frontend.vflow.base;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import consulo.hub.frontend.vflow.auth.view.AdminUserView;
import consulo.hub.frontend.vflow.config.view.AdminConfigView;
import consulo.hub.frontend.vflow.dash.ui.DashboardView;
import consulo.hub.frontend.vflow.errorReporter.view.AdminErrorReportsView;
import consulo.hub.frontend.vflow.errorReporter.view.ErrorReportsView;
import consulo.hub.frontend.vflow.repository.view.AdminRepositoryView;
import consulo.hub.frontend.vflow.repository.view.RepositoryView;
import consulo.hub.frontend.vflow.statistics.view.AdminStatisticsView;
import consulo.hub.frontend.vflow.statistics.view.StatisticsView;
import consulo.hub.frontend.vflow.storage.view.StorageView;
import consulo.hub.frontend.vflow.user.view.UserSessionsView;
import consulo.hub.frontend.vflow.user.view.UserInfoView;
import consulo.hub.shared.auth.Roles;
import consulo.hub.shared.auth.SecurityUtil;
import consulo.hub.shared.auth.domain.UserAccount;
import consulo.procoeton.core.service.LogoutService;
import consulo.procoeton.core.service.UserService;
import consulo.procoeton.core.vaadin.MainLayoutBase;
import consulo.procoeton.core.vaadin.ui.appnav.AppNav;
import consulo.procoeton.core.vaadin.ui.appnav.AppNavItem;
import org.springframework.beans.factory.annotation.Autowired;

@PreserveOnRefresh
@Uses(FontAwesome.Regular.Icon.class)
@Uses(FontAwesome.Solid.Icon.class)
@Uses(FontAwesome.Brands.Icon.class)
@JsModule("@vaadin/vaadin-lumo-styles/presets/compact.js")
public class MainLayout extends MainLayoutBase
{
	@Autowired
	public MainLayout(AccessAnnotationChecker accessAnnotationChecker, UserService userService, LogoutService logoutService)
	{
		super(accessAnnotationChecker, userService, logoutService);
	}

	@Override
	public String getHeaderText()
	{
		return "hub.consulo.io";
	}

	@Override
	protected void updateMenuItems(AppNav appNav)
	{
		appNav.addItem("Repository", RepositoryView.class, FontAwesome.Solid.PLUG);

		if(SecurityUtil.isLoggedIn())
		{
			AppNavItem homeGroup = new AppNavItem("User");
			homeGroup.setExpanded(true);
			appNav.addItem(homeGroup);

			homeGroup.addItem("Dashboard", DashboardView.class, FontAwesome.Solid.CHART_BAR);
			homeGroup.addItem("Error Reports", ErrorReportsView.class, FontAwesome.Solid.BOLT);
			homeGroup.addItem("Statistics", StatisticsView.class, FontAwesome.Solid.SIGNAL);
			homeGroup.addItem("Storage", StorageView.class, FontAwesome.Solid.FOLDER_OPEN);
			homeGroup.addItem("Sessions", UserSessionsView.class, FontAwesome.Solid.KEY);
		}

		//nav.addItem("Error Reports", StubView.class, FontAwesome.Solid.BOLT);

		if(SecurityUtil.hasRole(Roles.ROLE_SUPERUSER))
		{
			AppNavItem adminGroup = new AppNavItem("Administration");
			adminGroup.setExpanded(true);
			appNav.addItem(adminGroup);
			adminGroup.addItem("Users", AdminUserView.class, FontAwesome.Solid.USERS);
			adminGroup.addItem("Error Reports", AdminErrorReportsView.class, FontAwesome.Solid.BOLT);
			adminGroup.addItem("Statistics", AdminStatisticsView.class, FontAwesome.Solid.SIGNAL);
			adminGroup.addItem("Repository", AdminRepositoryView.class, FontAwesome.Solid.PLUG);
			adminGroup.addItem("Config", AdminConfigView.class, FontAwesome.Solid.WRENCH);
		}
	}

	@Override
	protected void updateUserMenu(SubMenu subMenu, UserAccount userAccount)
	{
		subMenu.addItem("Profile", event -> UI.getCurrent().navigate(UserInfoView.class));
	}
}