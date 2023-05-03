package consulo.hub.frontend.vflow.base;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.dom.ThemeList;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.theme.lumo.LumoUtility;
import consulo.hub.frontend.vflow.StubView;
import consulo.hub.frontend.vflow.auth.view.AdminUserView;
import consulo.hub.frontend.vflow.auth.view.OAuthKeysView;
import consulo.hub.frontend.vflow.base.appnav.AppNav;
import consulo.hub.frontend.vflow.base.appnav.AppNavItem;
import consulo.hub.frontend.vflow.config.view.AdminConfigView;
import consulo.hub.frontend.vflow.dash.ui.DashboardView;
import consulo.hub.frontend.vflow.errorReporter.view.AdminErrorReportsView;
import consulo.hub.frontend.vflow.errorReporter.view.ErrorReportsView;
import consulo.hub.frontend.vflow.repository.view.AdminRepositoryView;
import consulo.hub.frontend.vflow.repository.view.RepositoryView;
import consulo.hub.frontend.vflow.service.UserService;
import consulo.hub.frontend.vflow.storage.view.StorageView;
import consulo.hub.shared.auth.Roles;
import consulo.hub.shared.auth.SecurityUtil;
import consulo.hub.shared.auth.domain.UserAccount;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import java.util.Optional;

@PreserveOnRefresh
@Uses(FontAwesome.Regular.Icon.class)
@Uses(FontAwesome.Solid.Icon.class)
@Uses(FontAwesome.Brands.Icon.class)
@JsModule("@vaadin/vaadin-lumo-styles/presets/compact.js")
public class MainLayout extends AppLayout implements AfterNavigationObserver, BeforeEnterObserver
{
	private H2 myViewTitle;

	private HorizontalLayout myTopLayout;
	private HorizontalLayout myCustomizedTopLayout;

	private final AccessAnnotationChecker myAccessAnnotationChecker;
	private final UserService myUserService;

	private final Footer myFooter;
	private final AppNav myAppNav;

	public MainLayout(AccessAnnotationChecker accessAnnotationChecker, UserService userService)
	{
		myAccessAnnotationChecker = accessAnnotationChecker;
		myUserService = userService;

		setPrimarySection(Section.DRAWER);

		myFooter = new Footer();
		myAppNav = new AppNav();

		addDrawerContent();
		addHeaderContent();
	}

	private void addHeaderContent()
	{
		DrawerToggle toggle = new DrawerToggle();
		toggle.getElement().setAttribute("aria-label", "Menu toggle");

		myViewTitle = new H2();
		myViewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

		myCustomizedTopLayout = new HorizontalLayout();
		myTopLayout = new HorizontalLayout(myViewTitle, myCustomizedTopLayout);
		myTopLayout.setWidthFull();
		myTopLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

		myTopLayout.setAlignSelf(FlexComponent.Alignment.END, myCustomizedTopLayout);
		myCustomizedTopLayout.addClassName(LumoUtility.Margin.Left.AUTO);
		myCustomizedTopLayout.addClassName(LumoUtility.Margin.Right.MEDIUM);

		addToNavbar(true, toggle, myTopLayout);
	}

	private void addDrawerContent()
	{
		H1 appName = new H1("hub.consulo.io");
		appName.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.AUTO);

		HorizontalLayout layout = new HorizontalLayout(appName);
		layout.setWidthFull();
		layout.setAlignItems(FlexComponent.Alignment.CENTER);

		Header header = new Header(layout);

		Scroller scroller = new Scroller(myAppNav);

		addToDrawer(header, scroller, myFooter);
	}

	private void updateMenuItems()
	{
		myAppNav.removeAllItems();

		myAppNav.addItem("Repository", RepositoryView.class, FontAwesome.Solid.PLUG);

		if(SecurityUtil.isLoggedIn())
		{
			AppNavItem homeGroup = new AppNavItem("User");
			homeGroup.setExpanded(true);
			myAppNav.addItem(homeGroup);

			homeGroup.addItem("Dashboard", DashboardView.class, FontAwesome.Solid.CHART_BAR);
			homeGroup.addItem("Error Reports", ErrorReportsView.class, FontAwesome.Solid.BOLT);
			homeGroup.addItem("Statistics", StubView.class, FontAwesome.Solid.SIGNAL);
			homeGroup.addItem("Storage", StorageView.class, FontAwesome.Solid.FOLDER_OPEN);
			homeGroup.addItem("OAuth Keys", OAuthKeysView.class, FontAwesome.Solid.KEY);
		}

		//nav.addItem("Error Reports", StubView.class, FontAwesome.Solid.BOLT);

		if(SecurityUtil.hasRole(Roles.ROLE_SUPERUSER))
		{
			AppNavItem adminGroup = new AppNavItem("Administration");
			adminGroup.setExpanded(true);
			myAppNav.addItem(adminGroup);
			adminGroup.addItem("Users", AdminUserView.class, FontAwesome.Solid.USERS);
			adminGroup.addItem("Error Reports", AdminErrorReportsView.class, FontAwesome.Solid.BOLT);
			adminGroup.addItem("Statistics", StubView.class, FontAwesome.Solid.SIGNAL);
			adminGroup.addItem("Repository", AdminRepositoryView.class, FontAwesome.Solid.PLUG);
			adminGroup.addItem("Config", AdminConfigView.class, FontAwesome.Solid.WRENCH);
		}
	}

	private void updateLoginInfo()
	{
		myFooter.removeAll();

		Optional<UserAccount> maybeUser = myUserService.getCurrentUser();
		if(maybeUser.isPresent())
		{
			UserAccount user = maybeUser.get();

			Avatar avatar = new Avatar(user.getUsername());
			avatar.setThemeName("xsmall");
			avatar.getElement().setAttribute("tabindex", "-1");

			MenuBar userMenu = new MenuBar();
			userMenu.setThemeName("tertiary-inline contrast");

			MenuItem userName = userMenu.addItem("");
			Div div = new Div();
			div.add(avatar);
			div.add(user.getUsername());
			div.add(new Icon("lumo", "dropdown"));
			div.getElement().getStyle().set("display", "flex");
			div.getElement().getStyle().set("align-items", "center");
			div.getElement().getStyle().set("gap", "var(--lumo-space-s)");
			userName.add(div);

			userName.getSubMenu().addItem("Toggle Theme", e ->
			{
				UI ui = UI.getCurrent();
				ThemeList themeList = ui.getElement().getThemeList();
				if(themeList.contains("dark"))
				{
					themeList.remove("dark");
				}
				else
				{
					themeList.add("dark");
				}
			});

			userName.getSubMenu().addItem("Sign Out", e ->
			{
				getUI().get().getPage().setLocation("/logout");
				SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
				logoutHandler.logout(VaadinServletRequest.getCurrent().getHttpServletRequest(), null, null);
			});

			myFooter.add(userMenu);
		}
		else
		{
			Anchor loginLink = new Anchor("login", "Sign in");
			myFooter.add(loginLink);
		}
	}

	@Override
	protected void afterNavigation()
	{
		super.afterNavigation();
		myViewTitle.setText(getCurrentPageTitle());
	}

	private String getCurrentPageTitle()
	{
		PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
		return title == null ? "" : title.value();
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event)
	{
		updateMenuItems();

		updateLoginInfo();

		myCustomizedTopLayout.removeAll();
	}

	@Override
	public void afterNavigation(AfterNavigationEvent afterNavigationEvent)
	{
		Component content = getContent();
		if(content instanceof ChildLayout childLayout)
		{
			Component headerRightComponent = childLayout.getHeaderRightComponent();
			if(headerRightComponent != null)
			{
				myCustomizedTopLayout.add(headerRightComponent);
			}
			
			childLayout.viewReady(afterNavigationEvent);
		}
	}
}
