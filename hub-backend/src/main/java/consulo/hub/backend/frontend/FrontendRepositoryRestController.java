package consulo.hub.backend.frontend;

import consulo.hub.backend.repository.PluginStatisticsService;
import consulo.hub.backend.repository.RepositoryChannelIterationService;
import consulo.hub.shared.repository.FrontPluginNode;
import consulo.hub.shared.repository.PluginChannel;
import consulo.hub.shared.repository.domain.RepositoryDownloadInfo;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author VISTALL
 * @since 22/08/2021
 */
@RestController
public class FrontendRepositoryRestController
{
	private final PluginStatisticsService myPluginStatisticsService;

	private final RepositoryChannelIterationService myPluginChannelIterationService;

	private final FrontendCacheService myFrontendCacheService;

	public FrontendRepositoryRestController(PluginStatisticsService pluginStatisticsService, RepositoryChannelIterationService pluginChannelIterationService, FrontendCacheService frontendCacheService)
	{
		myPluginStatisticsService = pluginStatisticsService;
		myPluginChannelIterationService = pluginChannelIterationService;
		myFrontendCacheService = frontendCacheService;
	}

	@RequestMapping("/api/private/repository/list")
	public Collection<FrontPluginNode> listPlugins()
	{
		return myFrontendCacheService.listPlugins();
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
