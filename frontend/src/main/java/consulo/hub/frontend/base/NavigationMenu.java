package consulo.hub.frontend.base;

import java.lang.reflect.Field;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import com.google.common.eventbus.Subscribe;
import com.intellij.openapi.util.Pair;
import com.vaadin.navigator.View;
import com.vaadin.server.Resource;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.themes.ValoTheme;
import consulo.hub.frontend.base.ui.event.AfterViewChangeEvent;
import consulo.hub.frontend.base.ui.util.VaadinUIUtil;

/**
 * @author VISTALL
 * @since 18-Apr-17
 */
public class NavigationMenu extends CustomComponent
{
	public static final class ValoMenuItemButton extends Button
	{
		private static final String STYLE_SELECTED = "selected";
		private Class<? extends View> myClazz;

		public ValoMenuItemButton(String text, Resource icon, ClickListener listener, Class<? extends View> clazz)
		{
			myClazz = clazz;
			setPrimaryStyleName("valo-menu-item");
			setIcon(icon);
			setCaption(text);
			addClickListener(listener);
		}

		@Subscribe
		public void afterViewChange(AfterViewChangeEvent event)
		{
			removeStyleName(STYLE_SELECTED);
			if(myClazz == event.getView().getClass())
			{
				addStyleName(STYLE_SELECTED);
			}
		}
	}

	private final CssLayout myMenuItemsLayout = new CssLayout();
	private final MenuBar myUserMenuBar = new MenuBar();

	public NavigationMenu()
	{
		setPrimaryStyleName("valo-menu");
		setId("dashboard-menu");
		setSizeUndefined();
		setCompositionRoot(buildContent());
	}

	private Component buildContent()
	{
		final CssLayout menuContent = new CssLayout();
		menuContent.addStyleName(ValoTheme.MENU_PART);
		menuContent.addStyleName("no-vertical-drag-hints");
		menuContent.addStyleName("no-horizontal-drag-hints");
		menuContent.setWidth(null);
		menuContent.setHeight("100%");

		menuContent.addComponent(buildTitle());

		myUserMenuBar.addStyleName("user-menu");
		menuContent.addComponent(myUserMenuBar);

		menuContent.addComponent(myMenuItemsLayout);

		return menuContent;
	}

	private Component buildTitle()
	{
		Label logo = new Label("<strong>consulo.io</strong> Hub", ContentMode.HTML);
		logo.setSizeUndefined();
		HorizontalLayout logoWrapper = new HorizontalLayout(logo);
		logoWrapper.setComponentAlignment(logo, Alignment.MIDDLE_CENTER);
		logoWrapper.addStyleName("valo-menu-title");
		return logoWrapper;
	}

	@Nonnull
	public MenuBar.MenuItem setUser(String name, Resource icon)
	{
		myUserMenuBar.removeItems();

		return myUserMenuBar.addItem(name, icon, null);
	}

	@Nonnull
	public Component addSeparator(String title)
	{
		HorizontalLayout layout = VaadinUIUtil.newHorizontalLayout();
		layout.setWidth(100, Unit.PERCENTAGE);
		layout.setHeight(100, Unit.PERCENTAGE);
		layout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);

		HorizontalLayout midLayout = VaadinUIUtil.newHorizontalLayout();
		midLayout.setWidth(100, Unit.PERCENTAGE);
		midLayout.setHeight(1, Unit.PIXELS);
		midLayout.addStyleName("line-separator");

		layout.addComponent(midLayout);
		Label label = new Label(title);
		label.addStyleName("menu-label");
		label.addStyleName(ValoTheme.LABEL_TINY);
		layout.addComponent(label);

		myMenuItemsLayout.addComponent(layout);
		return layout;
	}

	@Nonnull
	public Component addSeparator()
	{
		HorizontalLayout layout = VaadinUIUtil.newHorizontalLayout();
		layout.setWidth(100, Unit.PERCENTAGE);
		layout.setHeight(100, Unit.PERCENTAGE);
		layout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);

		HorizontalLayout midLayout = VaadinUIUtil.newHorizontalLayout();
		midLayout.setWidth(100, Unit.PERCENTAGE);
		midLayout.setHeight(1, Unit.PIXELS);
		midLayout.addStyleName("line-separator");

		layout.addComponent(midLayout);

		layout.setExpandRatio(midLayout, 1f);
		myMenuItemsLayout.addComponent(layout);
		return layout;
	}

	private Component empty(int size)
	{
		HorizontalLayout midLayout = VaadinUIUtil.newHorizontalLayout();
		midLayout.setWidth(size, Unit.PIXELS);
		return midLayout;
	}

	@Nonnull
	public Component addNavigation(@Nonnull String name, @Nonnull Resource resource, @Nonnull Class<? extends View> clazz)
	{
		try
		{
			Field idField = clazz.getDeclaredField("ID");

			String value = (String) idField.get(null);

			Component menuItemComponent = new ValoMenuItemButton(name, resource, event -> getUI().getNavigator().navigateTo(value), clazz);

			RootUI.register(menuItemComponent);

			myMenuItemsLayout.addComponent(menuItemComponent);
			return menuItemComponent;
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	@Nonnull
	public Pair<Component, Consumer<Integer>> addNavigationWithBadge(@Nonnull String name, @Nonnull Resource resource, @Nonnull Class<? extends View> clazz)
	{
		try
		{
			Field idField = clazz.getDeclaredField("ID");

			String value = (String) idField.get(null);

			Component menuItemComponent = new ValoMenuItemButton(name, resource, event -> getUI().getNavigator().navigateTo(value), clazz);

			RootUI.register(menuItemComponent);

			Label label = new Label();
			Component component = buildForBadge(menuItemComponent, label);
			myMenuItemsLayout.addComponent(component);
			Consumer<Integer> consumer = integer ->
			{
				label.setVisible(integer > 0);
				label.setValue(String.valueOf(integer));
			};
			return Pair.create(component, consumer);
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	private static Component buildForBadge(Component menuItemButton, Component badgeLabel)
	{
		CssLayout dashboardWrapper = new CssLayout(menuItemButton);
		dashboardWrapper.addStyleName("nav-menu-badge");
		dashboardWrapper.addStyleName(ValoTheme.MENU_ITEM);
		badgeLabel.addStyleName(ValoTheme.MENU_BADGE);
		badgeLabel.setWidthUndefined();
		badgeLabel.setVisible(false);
		dashboardWrapper.addComponent(badgeLabel);
		return dashboardWrapper;
	}

	public void removeMenuItem(@Nonnull Component component)
	{
		myMenuItemsLayout.removeComponent(component);
	}
}
