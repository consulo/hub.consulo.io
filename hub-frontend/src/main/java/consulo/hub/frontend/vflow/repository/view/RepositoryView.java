package consulo.hub.frontend.vflow.repository.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import consulo.hub.frontend.vflow.PropertiesServiceImpl;
import consulo.hub.frontend.vflow.backend.service.BackendPluginStatisticsService;
import consulo.hub.frontend.vflow.backend.service.BackendRepositoryService;
import consulo.hub.frontend.vflow.base.MainLayout;
import consulo.procoeton.core.vaadin.ui.VChildLayout;
import consulo.procoeton.core.vaadin.ui.util.VaadinUIUtil;
import consulo.hub.frontend.vflow.repository.ui.RepositoryChannelPanel;
import consulo.hub.frontend.vflow.repository.ui.TagsLocalizeLoader;
import consulo.procoeton.core.vaadin.util.RouterUtil;
import consulo.hub.shared.repository.PluginChannel;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * @author VISTALL
 * @since 12-Mar-17
 */
@Route(value = "repository/:channel?/:id?", layout = MainLayout.class)
@PageTitle("Repository")
@AnonymousAllowed
public class RepositoryView extends VChildLayout
{
	public static final PluginChannel DEFAULT_CHANNEL = PluginChannel.release;

	public static final String CHANNEL = "channel";
	public static final String ID = "id";

	private final BackendRepositoryService myBackendRepositoryService;
	private final BackendPluginStatisticsService myBackendPluginStatisticsService;
	private final TagsLocalizeLoader myTagsLocalizeLoader;
	private final PropertiesServiceImpl myPropertiesService;

	private Component myLastRepositoryView;

	private Select<PluginChannel> myPluginChannelSelect;

	private final Component myHeaderComponent;

	private boolean isRendering;

	@Autowired
	public RepositoryView(PropertiesServiceImpl propertiesService,
						  BackendRepositoryService backendRepositoryService,
						  BackendPluginStatisticsService backendPluginStatisticsService,
						  TagsLocalizeLoader tagsLocalizeLoader)
	{
		myPropertiesService = propertiesService;
		myBackendRepositoryService = backendRepositoryService;
		myBackendPluginStatisticsService = backendPluginStatisticsService;
		myTagsLocalizeLoader = tagsLocalizeLoader;

		myPluginChannelSelect = new Select<>();
		myPluginChannelSelect.setItems(PluginChannel.values());
		myHeaderComponent = VaadinUIUtil.labeled("Channel: ", myPluginChannelSelect);

		myPluginChannelSelect.addValueChangeListener(event ->
		{
			if(isRendering)
			{
				return;
			}

			PluginChannel value = event.getValue();

			String id = myRouteParameters.get(ID).orElse(null);

			rebuild(value, id);

			RouterUtil.updateUrl(RepositoryView.class, () -> myRouteParameters, Map.of(CHANNEL, value.name()));
		});
	}

	@Override
	public Component getHeaderRightComponent()
	{
		return myHeaderComponent;
	}

	@Override
	public void viewReady(AfterNavigationEvent afterNavigationEvent)
	{
		if(myPropertiesService.isNotInstalled())
		{
			return;
		}

		String channelStr = myRouteParameters.get(CHANNEL).orElse(DEFAULT_CHANNEL.name());
		String id = myRouteParameters.get(ID).orElse(null);

		PluginChannel pluginChannel = null;
		try
		{
			pluginChannel = PluginChannel.valueOf(channelStr);
		}
		catch(IllegalArgumentException e)
		{
			pluginChannel = DEFAULT_CHANNEL;
		}

		isRendering = true;
		myPluginChannelSelect.setValue(pluginChannel);
		isRendering = false;

		rebuild(pluginChannel, id);
	}

	private void rebuild(PluginChannel pluginChannel, String id)
	{
		if(myLastRepositoryView != null)
		{
			remove(myLastRepositoryView);
		}

		RepositoryChannelPanel repositoryChannelPanel = new RepositoryChannelPanel(pluginChannel, myBackendRepositoryService, myBackendPluginStatisticsService, myTagsLocalizeLoader,
				myRouteParameters);

		add(repositoryChannelPanel);

		repositoryChannelPanel.selectPlugin(id);

		myLastRepositoryView = repositoryChannelPanel;
	}
}
