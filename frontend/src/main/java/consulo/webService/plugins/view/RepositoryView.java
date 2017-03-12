package consulo.webService.plugins.view;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.util.Pair;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.shared.ui.MarginInfo;
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
import consulo.webService.ui.util.TidyComponents;
import consulo.webService.ui.util.VaadinUIUtil;

/**
 * @author VISTALL
 * @since 12-Mar-17
 */
public class RepositoryView extends VerticalLayout implements View
{
	public static final String ID = "repo";

	private UserConfigurationService myUserConfigurationService;

	private PluginStatisticsService myPluginStatisticsService;

	private final PluginChannel myPluginChannel;
	private final RepositoryChannelPanel myRepositoryChannelPanel;

	public RepositoryView(UserConfigurationService userConfigurationService, PluginStatisticsService pluginStatisticsService, PluginChannel channel, String pluginId)
	{
		myUserConfigurationService = userConfigurationService;
		myPluginStatisticsService = pluginStatisticsService;
		myPluginChannel = channel;
		setMargin(new MarginInfo(false, false, false, false));

		setSizeFull();

		HorizontalLayout headerLayout = new HorizontalLayout();
		headerLayout.setWidth(100, Unit.PERCENTAGE);
		headerLayout.addStyleName("headerMargin");
		addComponent(headerLayout);

		Label header = new Label("Repository");
		headerLayout.addComponent(header);

		ComboBox channelBox = TidyComponents.newComboBox();
		for(PluginChannel t : PluginChannel.values())
		{
			channelBox.addItem(t);
			channelBox.setItemCaption(t, t.name());
		}
		Component labeled = VaadinUIUtil.labeled("Channel: ", channelBox);
		headerLayout.addComponent(labeled);
		headerLayout.setComponentAlignment(labeled, Alignment.MIDDLE_RIGHT);

		channelBox.setValue(myPluginChannel);

		addComponent(myRepositoryChannelPanel = new RepositoryChannelPanel(myPluginChannel, myUserConfigurationService, myPluginStatisticsService));

		setExpandRatio(myRepositoryChannelPanel, 1);

		channelBox.addValueChangeListener(event ->
		{
			PluginChannel value = (PluginChannel) event.getProperty().getValue();

			getUI().getNavigator().navigateTo(ID + "/" + value);
		});
	}

	@Override
	public void enter(ViewChangeListener.ViewChangeEvent viewChangeEvent)
	{
		myRepositoryChannelPanel.selectPlugin(viewChangeEvent.getParameters());
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
