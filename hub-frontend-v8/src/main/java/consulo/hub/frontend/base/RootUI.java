package consulo.hub.frontend.base;

import com.google.common.eventbus.EventBus;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.DefaultErrorHandler;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.shared.communication.PushMode;
import com.vaadin.flow.shared.ui.Transport;
import com.vaadin.flow.theme.Theme;
import consulo.hub.frontend.PropertiesService;
import consulo.hub.frontend.auth.view.*;
import consulo.hub.frontend.backend.service.BackendPluginStatisticsService;
import consulo.hub.frontend.backend.service.BackendRepositoryService;
import consulo.hub.frontend.base.ui.event.AfterViewChangeEvent;
import consulo.hub.frontend.config.view.AdminConfigView;
import consulo.hub.frontend.dash.view.DashboardView;
import consulo.hub.frontend.errorReporter.view.AdminErrorReportsView;
import consulo.hub.frontend.errorReporter.view.ErrorReportsView;
import consulo.hub.frontend.errorReporter.view.ErrorStatisticsView;
import consulo.hub.frontend.linkConsulo.LinkConsuloView;
import consulo.hub.frontend.repository.ui.TagsLocalizeLoader;
import consulo.hub.frontend.repository.view.AdminRepositoryView;
import consulo.hub.frontend.repository.view.RepositoryView;
import consulo.hub.frontend.statistics.view.AdminStatisticsView;
import consulo.hub.frontend.statistics.view.StatisticsView;
import consulo.hub.frontend.storage.view.StorageView;
import consulo.hub.frontend.util.GAPropertyKeys;
import consulo.hub.frontend.util.PropertySet;
import consulo.hub.shared.auth.Roles;
import consulo.hub.shared.auth.SecurityUtil;
import consulo.hub.shared.repository.PluginChannel;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.vaadin.googleanalytics.tracking.GoogleAnalyticsTracker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@SpringUI
@Widgetset("consulo.hub.frontend.WidgetSet")
@Push(value = PushMode.AUTOMATIC, transport = Transport.WEBSOCKET)
@Theme("tests-valo-metro")
@StyleSheet("https://fonts.googleapis.com/css?family=Roboto")
public class RootUI extends UI
{
	public static void register(Object o)
	{
		UI ui = getCurrent();
		((RootUI) ui).eventBus.register(o);
	}

	public static void unregister(Object o)
	{
		UI ui = getCurrent();
		((RootUI) ui).eventBus.unregister(o);
	}

	@Autowired
	private SpringViewProvider myViewProvider;

	@Autowired
	private ErrorView errorView;

	@Autowired
	private PropertiesService myPropertiesService;

	@Autowired
	private BackendRepositoryService myBackendRepositoryService;

	@Autowired
	private BackendPluginStatisticsService myBackendPluginStatisticsService;

	@Autowired
	private ApplicationContext myApplicationContext;

	@Autowired
	private TagsLocalizeLoader myTagsLocalizeLoader;

	private final NavigationMenu myNavigationMenu = new NavigationMenu();

	private final List<Component> myUnstableButtons = new ArrayList<>();

	private GoogleAnalyticsTracker myAnalyticsTracker;

	private final Map<PluginChannel, View> myRepositoryViewCache = new ConcurrentHashMap<>();

	private boolean myNotificationShow;

	private EventBus eventBus = new EventBus();

	@Override
	protected void init(VaadinRequest request)
	{
		if(!myNotificationShow)
		{
			myNotificationShow = true;
			Notification notification = new Notification("Welcome", "<b>hub.consulo.io</b> in beta stage, many features not implemented yet", Notification.Type.TRAY_NOTIFICATION);
			notification.setHtmlContentAllowed(true);
			notification.setDelayMsec((int) TimeUnit.SECONDS.toMillis(30));
			notification.show(getPage());
		}

		getPage().setTitle("Hub");

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		// todo [vistall] notifications
		Pair<Component, Consumer<Integer>> dashboardItem = myNavigationMenu.addNavigationWithBadge("Dashboard", FontAwesome.HOME, DashboardView.class);

		myNavigationMenu.addNavigation("Error Reports", FontAwesome.BOLT, ErrorReportsView.class);
		myNavigationMenu.addNavigation("Statistics", FontAwesome.SIGNAL, StatisticsView.class);
		myNavigationMenu.addNavigation("Storage", FontAwesome.FOLDER_OPEN, StorageView.class);
		myNavigationMenu.addNavigation("OAuth Keys", FontAwesome.KEY, OAuthKeysView.class);
		myNavigationMenu.addSeparator();
		myNavigationMenu.addNavigation("Repository", FontAwesome.PLUG, RepositoryView.class);
		myNavigationMenu.addSeparator("statistics");
		myNavigationMenu.addNavigation("Error Reports", FontAwesome.BOLT, ErrorStatisticsView.class);

		updateSideMenu(authentication);

		if(!myPropertiesService.isNotInstalled())
		{
			PropertySet propertySet = myPropertiesService.getPropertySet();

			String trackerId = propertySet.getStringProperty(GAPropertyKeys.TRACKER_ID);
			if(!StringUtil.isEmptyOrSpaces(trackerId))
			{
				myAnalyticsTracker = new GoogleAnalyticsTracker(trackerId, propertySet.getStringProperty(GAPropertyKeys.DOMAIN_NAME));

				addExtension(myAnalyticsTracker);
			}
		}

		RootContentView rootContentView = new RootContentView(myNavigationMenu);

		setContent(rootContentView);

		setErrorHandler(this::handleError);

		Navigator navigator = new Navigator(getUI(), rootContentView.getComponentContainer());
		navigator.addViewChangeListener(new ViewChangeListener()
		{
			@Override
			public boolean beforeViewChange(ViewChangeEvent event)
			{
				return true;
			}

			@Override
			public void afterViewChange(ViewChangeEvent event)
			{
				if(myAnalyticsTracker != null)
				{
					myAnalyticsTracker.trackPageview("/#!" + event.getViewName());
				}

				eventBus.post(new AfterViewChangeEvent(event.getNewView()));
			}
		});

		navigator.addProvider(myViewProvider);

		navigator.addProvider(new ViewProvider()
		{
			@Override
			public String getViewName(String viewAndParameters)
			{
				if(viewAndParameters.startsWith(LinkConsuloView.ID))
				{
					return LinkConsuloView.ID;
				}

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
				if(myPropertiesService.isNotInstalled())
				{
					return myApplicationContext.getBean(AccessDeniedView.class);
				}

				if(viewName.startsWith(LinkConsuloView.ID))
				{
					return myViewProvider.getView(LinkConsuloView.ID);
				}

				if(viewName.startsWith(RepositoryView.ID))
				{
					Pair<PluginChannel, String> pair = RepositoryView.parseViewParameters(viewName);
					return myRepositoryViewCache.computeIfAbsent(pair.getFirst(), it -> new RepositoryView(myPropertiesService, myBackendRepositoryService, myBackendPluginStatisticsService, it,
							myTagsLocalizeLoader));
				}
				return null;
			}
		});
		navigator.setErrorView(errorView);
		myViewProvider.setAccessDeniedViewClass(AccessDeniedView.class);
	}

	private void updateSideMenu(Authentication authentication)
	{
		String email = authentication == null ? "anonymous@anonymous" : authentication.getName();

		String emailHash = DigestUtils.md5Hex(email.toLowerCase().trim());

		String url = "https://www.gravatar.com/avatar/" + emailHash + ".png?s=" + 128 + "&d=mm";
		MenuBar.MenuItem menuItem = myNavigationMenu.setUser(email, new ExternalResource(url));
		if(authentication != null)
		{
			menuItem.addItem("Edit Profile", (MenuBar.Command) selectedItem -> getNavigator().navigateTo(UserInfoView.ID));
			menuItem.addSeparator();
			menuItem.addItem("Sign Out", (MenuBar.Command) selectedItem -> logout());
		}

		for(Component button : myUnstableButtons)
		{
			myNavigationMenu.removeMenuItem(button);
		}

		myUnstableButtons.clear();

		if(SecurityUtil.hasRole(Roles.ROLE_SUPERUSER))
		{
			myUnstableButtons.add(myNavigationMenu.addSeparator("admin"));
			myUnstableButtons.add(myNavigationMenu.addNavigation("Users", FontAwesome.USERS, AdminUserView.class));
			myUnstableButtons.add(myNavigationMenu.addNavigation("Error Reports", FontAwesome.BOLT, AdminErrorReportsView.class));
			myUnstableButtons.add(myNavigationMenu.addNavigation("Statistics", FontAwesome.SIGNAL, AdminStatisticsView.class));
			myUnstableButtons.add(myNavigationMenu.addNavigation("Repository", FontAwesome.PLUG, AdminRepositoryView.class));
			myUnstableButtons.add(myNavigationMenu.addNavigation("Config", FontAwesome.WRENCH, AdminConfigView.class));
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

		VaadinSession.getCurrent().close();

		Page.getCurrent().reload();
	}

	private void handleError(com.vaadin.server.ErrorEvent event)
	{
		Throwable t = DefaultErrorHandler.findRelevantThrowable(event.getThrowable());
		if(t instanceof AccessDeniedException)
		{
			Notification.show("You do not have permission to perform this operation", NotificationType.WARNING_MESSAGE);
		}
		else
		{
			DefaultErrorHandler.doDefault(event);
		}
	}
}
