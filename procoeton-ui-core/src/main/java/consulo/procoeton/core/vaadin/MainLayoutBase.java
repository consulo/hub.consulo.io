package consulo.procoeton.core.vaadin;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
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
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.theme.lumo.LumoUtility;
import consulo.hub.shared.auth.domain.UserAccount;
import consulo.procoeton.core.service.LogoutService;
import consulo.procoeton.core.service.UserService;
import consulo.procoeton.core.vaadin.ui.ChildLayout;
import consulo.procoeton.core.vaadin.ui.appnav.AppNav;
import consulo.procoeton.core.vaadin.view.login.LoginView;

import java.util.Optional;

@PreserveOnRefresh
@Uses(FontAwesome.Regular.Icon.class)
@Uses(FontAwesome.Solid.Icon.class)
@Uses(FontAwesome.Brands.Icon.class)
@JsModule("@vaadin/vaadin-lumo-styles/presets/compact.js")
public abstract class MainLayoutBase extends AppLayout implements AfterNavigationObserver, BeforeEnterObserver
{
	private H2 myViewTitle;

	private HorizontalLayout myTopLayout;
	private HorizontalLayout myCustomizedTopLayout;

	private final AccessAnnotationChecker myAccessAnnotationChecker;
	private final UserService myUserService;
	private final LogoutService myLogoutService;

	private final Footer myFooter;
	private final AppNav myAppNav;

	public MainLayoutBase(AccessAnnotationChecker accessAnnotationChecker, UserService userService, LogoutService logoutService)
	{
		myAccessAnnotationChecker = accessAnnotationChecker;
		myUserService = userService;
		myLogoutService = logoutService;

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

	public abstract String getHeaderText();

	private void addDrawerContent()
	{
		H1 appName = new H1(getHeaderText());
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

		updateMenuItems(myAppNav);
	}

	protected abstract void updateMenuItems(AppNav appNav);

	private void updateLoginInfo()
	{
		myFooter.removeAll();

		Component content = getContent();
		if(content instanceof LoginView)
		{
			return;
		}

		Optional<UserAccount> maybeUser = myUserService.getCurrentUser();
		if(maybeUser.isPresent())
		{
			UserAccount user = maybeUser.get();

			Avatar avatar = new Avatar(user.getUsername());
			avatar.setThemeName("xsmall");
			avatar.getElement().setAttribute("tabindex", "-1");

			MenuBar userMenu = new MenuBar();
			userMenu.setThemeName("tertiary-inline contrast");

			MenuItem userNameItem = userMenu.addItem("");
			Div div = new Div();
			div.add(avatar);
			div.add(user.getUsername());
			div.add(new Icon("lumo", "dropdown"));
			div.getElement().getStyle().set("display", "flex");
			div.getElement().getStyle().set("align-items", "center");
			div.getElement().getStyle().set("gap", "var(--lumo-space-s)");
			userNameItem.add(div);

			updateUserMenu(userNameItem.getSubMenu(), user);

			userNameItem.getSubMenu().addItem("Toggle Theme", e ->
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

			userNameItem.getSubMenu().addItem("Sign Out", e -> myLogoutService.logout(UI.getCurrent(), true));

			myFooter.add(userMenu);
		}
		else
		{
			Anchor loginLink = new Anchor("login", "Sign in");
			myFooter.add(loginLink);
		}
	}

	protected abstract void updateUserMenu(SubMenu subMenu, UserAccount userAccount);

	@Override
	protected void afterNavigation()
	{
		super.afterNavigation();
		myViewTitle.setText(getCurrentPageTitle());
		updateMenuItems();
		updateLoginInfo();
	}

	private String getCurrentPageTitle()
	{
		PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
		return title == null ? "" : title.value();
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event)
	{
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
