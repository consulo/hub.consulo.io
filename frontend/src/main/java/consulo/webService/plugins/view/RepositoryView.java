package consulo.webService.plugins.view;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.google.common.collect.Lists;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import consulo.webService.UserConfigurationService;
import consulo.webService.plugins.PluginChannel;
import consulo.webService.plugins.PluginStatisticsService;
import consulo.webService.plugins.ui.RepositoryChannelPanel;
import consulo.webService.ui.util.TinyComponents;
import consulo.webService.ui.util.VaadinUIUtil;

/**
 * @author VISTALL
 * @since 12-Mar-17
 */
public class RepositoryView extends VerticalLayout implements View
{
	public static final String ID = "repo";

	private final UserConfigurationService myUserConfigurationService;
	private final PluginStatisticsService myPluginStatisticsService;
	private final PluginChannel myChannel;

	public RepositoryView(UserConfigurationService userConfigurationService, PluginStatisticsService pluginStatisticsService, PluginChannel channel)
	{
		myUserConfigurationService = userConfigurationService;
		myPluginStatisticsService = pluginStatisticsService;
		myChannel = channel;

		setMargin(false);
		setSpacing(false);
		setSizeFull();
	}

	@Override
	public void enter(ViewChangeListener.ViewChangeEvent viewChangeEvent)
	{
		if(myUserConfigurationService.isNotInstalled())
		{
			return;
		}

		removeAllComponents();

		HorizontalLayout headerLayout = VaadinUIUtil.newHorizontalLayout();
		headerLayout.setWidth(100, Unit.PERCENTAGE);
		headerLayout.addStyleName("headerMargin");
		addComponent(headerLayout);

		Label header = new Label("Repository");
		headerLayout.addComponent(header);

		ComboBox<PluginChannel> channelBox = TinyComponents.newComboBox();
		channelBox.setEmptySelectionAllowed(false);
		channelBox.setDataProvider(new ListDataProvider<PluginChannel>(Lists.newArrayList(PluginChannel.values())));
		Component labeled = VaadinUIUtil.labeled("Channel: ", channelBox);
		headerLayout.addComponent(labeled);
		headerLayout.setComponentAlignment(labeled, Alignment.MIDDLE_RIGHT);

		channelBox.setValue(myChannel);

		RepositoryChannelPanel repositoryChannelPanel;
		addComponent(repositoryChannelPanel = new RepositoryChannelPanel(myChannel, myUserConfigurationService, myPluginStatisticsService));

		setExpandRatio(repositoryChannelPanel, 1);

		channelBox.addValueChangeListener(event ->
		{
			PluginChannel value = event.getValue();

			String selectedPluginId = repositoryChannelPanel.getSelectedPluginId();
			if(StringUtil.isEmpty(selectedPluginId))
			{
				getUI().getNavigator().navigateTo(ID + "/" + value);
			}
			else
			{
				getUI().getNavigator().navigateTo(ID + "/" + value + "/" + selectedPluginId);
			}
		});

		repositoryChannelPanel.selectPlugin(viewChangeEvent.getParameters());
	}

	@NotNull
	public static Pair<PluginChannel, String> parseViewParameters(@NotNull String fragment)
	{
		try
		{
			if(fragment.contains("/"))
			{
				String[] split = fragment.split("/");
				return Pair.create(PluginChannel.valueOf(split[1]), split.length == 3 ? split[2] : null);
			}
		}
		catch(Exception ignored)
		{
			//
		}
		return Pair.create(PluginChannel.release, null);
	}

	public static String getViewParameters(@NotNull PluginChannel pluginChannel, @Nullable String id)
	{
		if(id != null)
		{
			return pluginChannel.name() + "/" + id;
		}
		return pluginChannel.name();
	}
}
