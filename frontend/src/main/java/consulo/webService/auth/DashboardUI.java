package consulo.webService.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.annotations.Theme;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.DefaultErrorHandler;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.shared.communication.PushMode;
import com.vaadin.shared.ui.ui.Transport;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.spring.navigator.SpringViewProvider;
import com.vaadin.ui.Notification;
import consulo.webService.auth.mongo.service.UserService;
import consulo.webService.auth.ui.SideMenu;
import consulo.webService.auth.ui.SideMenuUI;
import consulo.webService.auth.view.AccessDeniedView;
import consulo.webService.auth.view.AdminErrorReportsView;
import consulo.webService.auth.view.DashboardView;
import consulo.webService.auth.view.ErrorReportsView;
import consulo.webService.auth.view.ErrorView;
import consulo.webService.auth.view.OAuthKeysView;
import consulo.webService.auth.view.UserInfoView;
import consulo.webService.ui.BaseUI;
import consulo.webService.ui.components.CaptchaFactory;

@SpringUI(path = "dash")
@SideMenuUI
// No @Push annotation, we are going to enable it programmatically when the user logs on
@Theme("tests-valo-metro")
@StyleSheet("https://fonts.googleapis.com/css?family=Roboto")
public class DashboardUI extends BaseUI
{
	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private SpringViewProvider viewProvider;

	@Autowired
	private ErrorView errorView;

	@Autowired
	private UserService myUserService;

	@Autowired
	private CaptchaFactory myCaptchaFactory;

	@Override
	protected void initImpl(VaadinRequest request, Page page)
	{
		page.setTitle("Dashboard");
		if(SecurityUtil.isLoggedIn())
		{
			buildUI();
		}
		else
		{
			notAuthorized();
		}
	}

	private void notAuthorized()
	{
		setContent(new LoginOrRegisterForm(myCaptchaFactory, this::login, this::register));
	}

	private void buildUI()
	{
		SideMenu sideMenu = new SideMenu();
		sideMenu.setMenuCaption("Hub");
		sideMenu.setUserIcon(FontAwesome.USER);
		sideMenu.setUserName(SecurityContextHolder.getContext().getAuthentication().getName());
		sideMenu.setUserNavigation(UserInfoView.ID);

		sideMenu.addNavigation("Dashboard", FontAwesome.HOME, DashboardView.ID);
		sideMenu.addNavigation("Error Reports", FontAwesome.BOLT, ErrorReportsView.ID);
		sideMenu.addNavigation("OAuth Keys", FontAwesome.KEY, OAuthKeysView.ID);

		if(SecurityUtil.hasRole(Roles.ROLE_ADMIN))
		{
			sideMenu.addNavigation("Admin / Error Reports", FontAwesome.BOLT, AdminErrorReportsView.ID);
		}

		sideMenu.addMenuItem("Logout", FontAwesome.SIGN_OUT, this::logout);

		setContent(sideMenu);

		setErrorHandler(this::handleError);

		Navigator navigator = new Navigator(this, sideMenu);
		navigator.addProvider(viewProvider);
		navigator.setErrorView(errorView);
		viewProvider.setAccessDeniedViewClass(AccessDeniedView.class);
	}

	private boolean login(String username, String password)
	{
		try
		{
			Authentication token = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
			// Reinitialize the session to protect against session fixation attacks. This does not work
			// with websocket communication.
			VaadinService.reinitializeSession(VaadinService.getCurrentRequest());
			SecurityContextHolder.getContext().setAuthentication(token);
			// Now when the session is reinitialized, we can enable websocket communication. Or we could have just
			// used WEBSOCKET_XHR and skipped this step completely.
			getPushConfiguration().setTransport(Transport.WEBSOCKET);
			getPushConfiguration().setPushMode(PushMode.AUTOMATIC);

			buildUI();

			getNavigator().navigateTo(getNavigator().getState());
			return true;
		}
		catch(AuthenticationException ex)
		{
			return false;
		}
	}

	private boolean register(String username, String password)
	{
		if(!myUserService.registerUser(username, password))
		{
			return false;
		}
		return login(username, password);
	}

	private void logout()
	{
		getPage().reload();
		getPage().setUriFragment(null, false);
		getSession().close();
	}

	private void handleError(com.vaadin.server.ErrorEvent event)
	{
		Throwable t = DefaultErrorHandler.findRelevantThrowable(event.getThrowable());
		if(t instanceof AccessDeniedException)
		{
			Notification.show("You do not have permission to perform this operation", Notification.Type.WARNING_MESSAGE);
		}
		else
		{
			DefaultErrorHandler.doDefault(event);
		}
	}
}
