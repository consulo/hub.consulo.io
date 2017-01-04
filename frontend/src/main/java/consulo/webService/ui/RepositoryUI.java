package consulo.webService.ui;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.annotations.Theme;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import consulo.webService.UserConfigurationService;
import consulo.webService.plugins.PluginChannel;
import consulo.webService.plugins.PluginStatisticsService;
import consulo.webService.plugins.ui.RepositoryChannelUI;
import consulo.webService.ui.util.TidyComponents;
import consulo.webService.ui.util.VaadinUIUtil;

/**
 * @author VISTALL
 * @since 09-Nov-16
 */
@SpringUI(path = "repo")
// No @Push annotation, we are going to enable it programmatically when the user logs on
@Theme("tests-valo-metro")
@StyleSheet("https://fonts.googleapis.com/css?family=Roboto")
public class RepositoryUI extends BaseUI
{
	@Autowired
	private UserConfigurationService myUserConfigurationService;

	@Autowired
	private PluginStatisticsService myPluginStatisticsService;

	private RepositoryChannelUI myRepositoryChannelUI;

	@Override
	protected void initImpl(VaadinRequest request, Page page)
	{
		getPage().setTitle("Platform & Plugin Repository");

		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.setSizeFull();

		HorizontalLayout headerLayout = new HorizontalLayout();
		headerLayout.addStyleName("backgroundGray");
		headerLayout.setMargin(true);
		headerLayout.setWidth(100, Unit.PERCENTAGE);
		verticalLayout.addComponent(headerLayout);

		ComboBox channelBox = TidyComponents.newComboBox();
		for(PluginChannel channel : PluginChannel.values())
		{
			channelBox.addItem(channel);
			channelBox.setItemCaption(channel, channel.name());
		}
		Component labeled = VaadinUIUtil.labeled("Channel: ", channelBox);
		headerLayout.addComponent(labeled);
		headerLayout.setComponentAlignment(labeled, Alignment.MIDDLE_RIGHT);

		channelBox.addValueChangeListener(event -> {
			if(myRepositoryChannelUI != null)
			{
				verticalLayout.removeComponent(myRepositoryChannelUI);
				myRepositoryChannelUI = null;
			}

			PluginChannel value = (PluginChannel) event.getProperty().getValue();

			String selectedPluginId = null;
			Page p = getUI().getPage();
			String uriFragment = p.getUriFragment();
			if(uriFragment != null)
			{
				Pair<PluginChannel, String> pair = parseUriFragment(getUI());

				p.setUriFragment(getUrlFragment(value, selectedPluginId = pair.getSecond()));
			}
			else
			{
				p.setUriFragment(getUrlFragment(value, null));
			}

			verticalLayout.addComponent(myRepositoryChannelUI = new RepositoryChannelUI(p, value, myUserConfigurationService, myPluginStatisticsService, selectedPluginId));
			verticalLayout.setExpandRatio(myRepositoryChannelUI, 1);
		});

		Pair<PluginChannel, String> pair = parseUriFragment(getUI());

		channelBox.setValue(pair.getFirst());

		setContent(verticalLayout);
	}

	@NotNull
	public static Pair<PluginChannel, String> parseUriFragment(@NotNull UI ui)
	{
		try
		{
			String fragment = ui.getPage().getUriFragment();
			if(!StringUtil.isEmpty(fragment))
			{
				if(fragment.contains(":"))
				{
					String[] split = fragment.split(":");
					return Pair.create(PluginChannel.valueOf(split[0]), split[1]);
				}
				return Pair.create(PluginChannel.valueOf(fragment), null);
			}
		}
		catch(Exception ignored)
		{
			//
		}
		return Pair.create(PluginChannel.release, null);
	}

	public static String getUrlFragment(@NotNull PluginChannel pluginChannel, @Nullable String id)
	{
		if(id != null)
		{
			return pluginChannel.name() + ":" + id;
		}
		return pluginChannel.name();
	}
}
