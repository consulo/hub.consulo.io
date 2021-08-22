package consulo.hub.backend.frontend;

import consulo.hub.backend.repository.PluginChannelService;
import consulo.hub.backend.repository.PluginChannelsService;
import consulo.hub.backend.repository.PluginStatisticsService;
import consulo.hub.shared.repository.PluginChannel;
import consulo.hub.shared.repository.PluginNode;
import consulo.hub.shared.repository.mongo.domain.MongoDownloadStat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @since 22/08/2021
 */
@RestController
public class FrontendRepositoryRestController
{
	@Autowired
	private PluginChannelsService myPluginChannelsService;

	@Autowired
	private PluginStatisticsService myPluginStatisticsService;

	@RequestMapping("/private/api/repository/list")
	public List<PluginNode> listPlugins(@RequestParam("channel") PluginChannel pluginChannel)
	{
		PluginChannelService service = myPluginChannelsService.getRepositoryByChannel(pluginChannel);

		List<PluginNode> pluginNodes = new ArrayList<>();
		service.iteratePluginNodes(pluginNodes::add);
		return pluginNodes;
	}

	@RequestMapping("/private/api/repository/downloadStat")
	public List<MongoDownloadStat> downloadStat(@RequestParam("pluginId") String pluginId)
	{
		return myPluginStatisticsService.getDownloadStat(pluginId);
	}
}
