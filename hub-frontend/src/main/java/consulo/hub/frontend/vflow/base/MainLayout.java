package consulo.hub.frontend.vflow.base;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
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
import consulo.hub.frontend.vflow.user.view.UserInfoView;
import consulo.hub.frontend.vflow.user.view.UserSessionsView;
import consulo.hub.shared.auth.Roles;
import consulo.hub.shared.auth.SecurityUtil;
import consulo.hub.shared.auth.domain.UserAccount;
import consulo.procoeton.core.service.LogoutService;
import consulo.procoeton.core.service.UserService;
import consulo.procoeton.core.vaadin.MainLayoutBase;
import org.springframework.beans.factory.annotation.Autowired;

@PreserveOnRefresh
@Uses(FontAwesome.Regular.Icon.class)
@Uses(FontAwesome.Solid.Icon.class)
@Uses(FontAwesome.Brands.Icon.class)
@JsModule("@vaadin/vaadin-lumo-styles/presets/compact.js")
public class MainLayout extends MainLayoutBase {
    @Autowired
    public MainLayout(AccessAnnotationChecker accessAnnotationChecker, UserService userService, LogoutService logoutService) {
        super(accessAnnotationChecker, userService, logoutService);
    }

    @Override
    public String getHeaderText() {
        return "hub.consulo.io";
    }

    @Override
    protected void updateMenuItems(SideNav appNav) {
        appNav.addItem(new SideNavItem("Repository", RepositoryView.class, FontAwesome.Solid.PLUG.create()));

        if (SecurityUtil.isLoggedIn()) {
            SideNavItem homeGroup = new SideNavItem("User");
            homeGroup.setExpanded(true);
            appNav.addItem(homeGroup);

            homeGroup.addItem(new SideNavItem("Dashboard", DashboardView.class, FontAwesome.Solid.CHART_BAR.create()));
            homeGroup.addItem(new SideNavItem("Error Reports", ErrorReportsView.class, FontAwesome.Solid.BOLT.create()));
            homeGroup.addItem(new SideNavItem("Statistics", StatisticsView.class, FontAwesome.Solid.SIGNAL.create()));
            homeGroup.addItem(new SideNavItem("Storage", StorageView.class, FontAwesome.Solid.FOLDER_OPEN.create()));
            homeGroup.addItem(new SideNavItem("Sessions", UserSessionsView.class, FontAwesome.Solid.KEY.create()));
        }

        //nav.addItem("Error Reports", StubView.class, FontAwesome.Solid.BOLT);

        if (SecurityUtil.hasRole(Roles.ROLE_SUPERUSER)) {
            SideNavItem adminGroup = new SideNavItem("Administration");
            adminGroup.setExpanded(true);
            appNav.addItem(adminGroup);
            adminGroup.addItem(new SideNavItem("Users", AdminUserView.class, FontAwesome.Solid.USERS.create()));
            adminGroup.addItem(new SideNavItem("Error Reports", AdminErrorReportsView.class, FontAwesome.Solid.BOLT.create()));
            adminGroup.addItem(new SideNavItem("Statistics", AdminStatisticsView.class, FontAwesome.Solid.SIGNAL.create()));
            adminGroup.addItem(new SideNavItem("Repository", AdminRepositoryView.class, FontAwesome.Solid.PLUG.create()));
            adminGroup.addItem(new SideNavItem("Config", AdminConfigView.class, FontAwesome.Solid.WRENCH.create()));
        }
    }

    @Override
    protected void updateUserMenu(SubMenu subMenu, UserAccount userAccount) {
        subMenu.addItem("Profile", event -> UI.getCurrent().navigate(UserInfoView.class));
    }
}
