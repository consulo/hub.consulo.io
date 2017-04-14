package consulo.webService.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.intellij.openapi.util.Pair;
import com.intellij.util.containers.FactoryMap;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.annotations.Theme;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewProvider;
import com.vaadin.server.DefaultErrorHandler;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.communication.PushMode;
import com.vaadin.shared.ui.ui.Transport;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.spring.navigator.SpringViewProvider;
import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import consulo.webService.UserConfigurationService;
import consulo.webService.auth.Roles;
import consulo.webService.auth.SecurityUtil;
import consulo.webService.auth.ui.SideMenu;
import consulo.webService.auth.ui.SideMenuUI;
import consulo.webService.auth.view.AccessDeniedView;
import consulo.webService.auth.view.AdminUserView;
import consulo.webService.auth.view.DashboardView;
import consulo.webService.auth.view.ErrorView;
import consulo.webService.auth.view.OAuthKeysView;
import consulo.webService.auth.view.UserInfoView;
import consulo.webService.config.view.AdminConfigView;
import consulo.webService.errorReporter.view.AdminErrorReportsView;
import consulo.webService.errorReporter.view.ErrorReportsView;
import consulo.webService.plugins.PluginChannel;
import consulo.webService.plugins.PluginStatisticsService;
import consulo.webService.plugins.view.AdminRepositoryView;
import consulo.webService.plugins.view.RepositoryView;
import consulo.webService.storage.view.StorageView;

@SpringUI
@SideMenuUI
@Push(value = PushMode.AUTOMATIC, transport = Transport.WEBSOCKET)
@Theme("tests-valo-metro")
@StyleSheet("https://fonts.googleapis.com/css?family=Roboto")
public class DashboardUI extends UI
{
	@Autowired
	private SpringViewProvider viewProvider;

	@Autowired
	private ErrorView errorView;

	@Autowired
	private UserConfigurationService myUserConfigurationService;

	@Autowired
	private PluginStatisticsService myPluginStatisticsService;

	@Autowired
	private ApplicationContext myApplicationContext;

	private SideMenu mySideMenu = new SideMenu();

	private final List<Button> myUnstableButtons = new ArrayList<>();

	private final Map<PluginChannel, View> myRepositoryViewCache = new FactoryMap<PluginChannel, View>()
	{
		public View create(PluginChannel c)
		{
			return new RepositoryView(myUserConfigurationService, myPluginStatisticsService, c);
		}
	};

	@Override
	protected void init(VaadinRequest request)
	{
		getPage().setTitle("Hub");

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		mySideMenu.setMenuCaption("Hub");
		mySideMenu.setUserIcon(FontAwesome.USER);
		mySideMenu.setUserNavigation(UserInfoView.ID);

		mySideMenu.addNavigation("Dashboard", FontAwesome.HOME, DashboardView.ID);
		mySideMenu.addNavigation("Error Reports", FontAwesome.BOLT, ErrorReportsView.ID);
		mySideMenu.addNavigation("Storage", FontAwesome.FOLDER_OPEN, StorageView.ID);
		mySideMenu.addNavigation("OAuth Keys", FontAwesome.KEY, OAuthKeysView.ID);
		mySideMenu.addNavigation("Repository", FontAwesome.PLUG, RepositoryView.ID);

		updateSideMenu(authentication);

		setContent(mySideMenu);

		setErrorHandler(this::handleError);

		Navigator navigator = new Navigator(this, mySideMenu);
		navigator.addProvider(viewProvider);
		navigator.addProvider(new ViewProvider()
		{
			@Override
			public String getViewName(String viewAndParameters)
			{
				if(viewAndParameters.startsWith(RepositoryView.ID))
				{
					Pair<PluginChannel, String> pair = RepositoryView.parseViewParameters(viewAndParameters);
					return RepositoryView.ID + "/" + RepositoryView.getViewParameters(pair.getFirst(), null);
				}
				return viewAndParameters;
			}

			@Override
			public View getView(String viewName)
			{
				if(myUserConfigurationService.isNotInstalled())
				{
					return myApplicationContext.getBean(AccessDeniedView.class);
				}

				if(viewName.startsWith(RepositoryView.ID))
				{
					Pair<PluginChannel, String> pair = RepositoryView.parseViewParameters(viewName);
					return myRepositoryViewCache.get(pair.getFirst());
				}
				return null;
			}
		});
		navigator.setErrorView(errorView);
		viewProvider.setAccessDeniedViewClass(AccessDeniedView.class);
	}

	private void updateSideMenu(Authentication authentication)
	{
		mySideMenu.setUserName(authentication == null ? "anonymous" : authentication.getName());
		for(Button button : myUnstableButtons)
		{
			mySideMenu.removeMenuItem(button);
		}

		myUnstableButtons.clear();

		if(SecurityUtil.hasRole(Roles.ROLE_ADMIN))
		{
			myUnstableButtons.add(mySideMenu.addNavigation("Admin | Users", FontAwesome.USERS, AdminUserView.ID));
			myUnstableButtons.add(mySideMenu.addNavigation("Admin | Error Reports", FontAwesome.BOLT, AdminErrorReportsView.ID));
			myUnstableButtons.add(mySideMenu.addNavigation("Admin | Repository", FontAwesome.PLUG, AdminRepositoryView.ID));
			myUnstableButtons.add(mySideMenu.addNavigation("Admin | Config", FontAwesome.WRENCH, AdminConfigView.ID));
		}

		if(authentication != null)
		{
			myUnstableButtons.add(mySideMenu.addMenuItem("Logout", FontAwesome.SIGN_OUT, this::logout));
		}
	}

	public void logged()
	{
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		updateSideMenu(authentication);
	}

	private void logout()
	{
		SecurityContextHolder.getContext().setAuthentication(null);

		getNavigator().navigateTo(getNavigator().getState());

		logged();
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
