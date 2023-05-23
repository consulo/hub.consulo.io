package consulo.hub.backend.frontend;

import consulo.hub.backend.repository.PluginChannelsIterationScheduler;
import consulo.hub.backend.repository.RepositoryChannelStore;
import consulo.hub.backend.repository.RepositoryChannelsService;
import consulo.hub.backend.repository.PluginStatisticsService;
import consulo.hub.shared.repository.PluginChannel;
import consulo.hub.shared.repository.PluginNode;
import consulo.hub.shared.repository.domain.RepositoryDownloadInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author VISTALL
 * @since 22/08/2021
 */
@RestController
public class FrontendRepositoryRestController
{
	@Autowired
	private RepositoryChannelsService myPluginChannelsService;

	@Autowired
	private PluginStatisticsService myPluginStatisticsService;

	@Autowired
	private PluginChannelsIterationScheduler myPluginChannelIterationService;

	@RequestMapping("/api/private/repository/list")
	public List<PluginNode> listPlugins(@RequestParam("channel") PluginChannel pluginChannel)
	{
		RepositoryChannelStore service = myPluginChannelsService.getRepositoryByChannel(pluginChannel);

		List<PluginNode> pluginNodes = new ArrayList<>();
		service.iteratePluginNodes(pluginNodes::add);
		Map<String, int[]> downloadStat = new HashMap<>();
		for(PluginNode node : pluginNodes)
		{
			int[] counts = downloadStat.computeIfAbsent(node.id, id ->
			{
				int count = myPluginStatisticsService.getDownloadStatCount(id, pluginChannel);
				int countAll = myPluginStatisticsService.getDownloadStatCountAll(id);
				return new int[] {count, countAll};
			});

			node.downloads = counts[0];
			node.downloadsAll = counts[1];
		}
		return pluginNodes;
	}

	@RequestMapping("/api/private/repository/downloadStat")
	public List<RepositoryDownloadInfo> downloadStat(@RequestParam("pluginId") String pluginId)
	{
		return myPluginStatisticsService.getDownloadStat(pluginId);
	}

	@RequestMapping("/api/private/repository/iterate")
	public Map<String, String> iteratePlugins(@RequestParam("from") PluginChannel from, @RequestParam("to") PluginChannel to)
	{
		myPluginChannelIterationService.iterate(from, to);
		return Map.of();
	}
}
