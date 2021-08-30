package consulo.hub.frontend.repository.view;

import com.google.common.collect.Lists;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.*;
import consulo.hub.frontend.PropertiesService;
import consulo.hub.frontend.backend.service.PluginChannelsService;
import consulo.hub.frontend.backend.service.BackendPluginStatisticsService;
import consulo.hub.frontend.base.ui.util.TinyComponents;
import consulo.hub.frontend.base.ui.util.VaadinUIUtil;
import consulo.hub.frontend.repository.ui.RepositoryChannelPanel;
import consulo.hub.shared.repository.PluginChannel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 12-Mar-17
 */
public class RepositoryView extends VerticalLayout implements View
{
	public static final String ID = "repo";

	private final PluginChannelsService myPluginChannelsService;
	private final BackendPluginStatisticsService myBackendPluginStatisticsService;
	private final PluginChannel myChannel;
	private final PropertiesService myPropertiesService;

	public RepositoryView(PropertiesService propertiesService, PluginChannelsService pluginChannelsService, BackendPluginStatisticsService backendPluginStatisticsService, PluginChannel channel)
	{
		myPropertiesService = propertiesService;
		myPluginChannelsService = pluginChannelsService;
		myBackendPluginStatisticsService = backendPluginStatisticsService;
		myChannel = channel;

		setMargin(false);
		setSpacing(false);
		setSizeFull();
	}

	@Override
	public void enter(ViewChangeListener.ViewChangeEvent viewChangeEvent)
	{
		if(myPropertiesService.isNotInstalled())
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
		addComponent(repositoryChannelPanel = new RepositoryChannelPanel(myChannel, myPluginChannelsService, myBackendPluginStatisticsService));

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

	@Nonnull
	public static Pair<PluginChannel, String> parseViewParameters(@Nonnull String fragment)
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

	public static String getViewParameters(@Nonnull PluginChannel pluginChannel, @Nullable String id)
	{
		if(id != null)
		{
			return pluginChannel.name() + "/" + id;
		}
		return pluginChannel.name();
	}
}
