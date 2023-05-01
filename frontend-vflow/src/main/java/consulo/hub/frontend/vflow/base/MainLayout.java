package consulo.hub.frontend.vflow.base;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.theme.lumo.LumoUtility;
import consulo.hub.frontend.vflow.StubView;
import consulo.hub.frontend.vflow.base.appnav.AppNav;
import consulo.hub.frontend.vflow.dash.ui.DashboardView;
import consulo.hub.frontend.vflow.repository.view.RepositoryView;
import consulo.hub.frontend.vflow.service.UserService;
import consulo.hub.frontend.vflow.storage.view.StorageView;
import consulo.hub.shared.auth.domain.UserAccount;

import java.util.Optional;

@PreserveOnRefresh
@Uses(FontAwesome.Regular.Icon.class)
@Uses(FontAwesome.Solid.Icon.class)
@Uses(FontAwesome.Brands.Icon.class)
public class MainLayout extends AppLayout implements AfterNavigationObserver, BeforeEnterObserver
{
	private H2 myViewTitle;

	private HorizontalLayout myTopLayout;
	private HorizontalLayout myCustomizedTopLayout;

	private final AccessAnnotationChecker myAccessAnnotationChecker;
	private final UserService myUserService;

	public MainLayout(AccessAnnotationChecker accessAnnotationChecker, UserService userService)
	{
		myAccessAnnotationChecker = accessAnnotationChecker;
		myUserService = userService;
		setPrimarySection(Section.DRAWER);
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
		appName.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);
		Header header = new Header(appName);

		Scroller scroller = new Scroller(createNavigation());

		addToDrawer(header, scroller, createFooter());
	}

	private AppNav createNavigation()
	{
		AppNav nav = new AppNav();

		nav.addItem("Dashboard", DashboardView.class, VaadinIcon.HOME);
		nav.addItem("Error Reports", StubView.class, FontAwesome.Solid.BOLT);
		nav.addItem("Statistics", StubView.class, FontAwesome.Solid.SIGNAL);
		nav.addItem("Storage", StorageView.class, FontAwesome.Solid.FOLDER_OPEN);
		nav.addItem("OAuth Keys", StubView.class, FontAwesome.Solid.KEY);
		nav.addSeparator();
		nav.addItem("Repository", RepositoryView.class, FontAwesome.Solid.PLUG);
		nav.addSeparator("statistics");
		nav.addItem("Error Reports", StubView.class, FontAwesome.Solid.BOLT);

		return nav;
	}

	private Footer createFooter()
	{
		Footer layout = new Footer();

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
			userName.getSubMenu().addItem("Sign out", e -> myUserService.logout());

			layout.add(userMenu);
		}
		else
		{
			Anchor loginLink = new Anchor("login", "Sign in");
			layout.add(loginLink);
		}

		return layout;
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
