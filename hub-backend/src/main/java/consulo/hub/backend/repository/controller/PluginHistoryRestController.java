package consulo.hub.backend.repository.controller;

import consulo.hub.backend.repository.PluginHistoryRequest;
import consulo.hub.backend.repository.PluginHistoryResponse;
import consulo.hub.backend.repository.PluginHistoryService;
import consulo.hub.backend.repository.RestPluginHistoryEntry;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author VISTALL
 * @since 05/11/2021
 */
@RestController
public class PluginHistoryRestController
{
	private final PluginHistoryService myPluginHistoryService;

	public PluginHistoryRestController(PluginHistoryService pluginHistoryService)
	{
		myPluginHistoryService = pluginHistoryService;
	}

	@RequestMapping("/api/repository/history/listByVersion")
	public List<RestPluginHistoryEntry> listPluginHistory(@RequestParam("id") String pluginId, @RequestParam("version") String pluginVersion)
	{
		return myPluginHistoryService.listPluginHistory(pluginId, pluginVersion).toList();
	}

	@RequestMapping("/api/repository/history/listByVersionRange")
	public List<RestPluginHistoryEntry> listPluginHistoryByRange(@RequestParam("id") String pluginId,
																 @RequestParam("fromVersion") String fromVer,
																 @RequestParam("toVersion") String toVer,
																 @RequestParam(value = "includeFromVersion", required = false, defaultValue = "true") boolean includeFromVersion)
	{
		return myPluginHistoryService.listPluginHistoryByRange(pluginId, fromVer, toVer, includeFromVersion).toList();
	}

	@PostMapping("/api/repository/history/request")
	@ResponseBody
	public PluginHistoryResponse requestHistory(@RequestBody PluginHistoryRequest request)
	{
		if(request.plugins.length == 0)
		{
			throw new IllegalArgumentException("Empty plugins");
		}

		PluginHistoryResponse response = new PluginHistoryResponse();

		for(PluginHistoryRequest.PluginInfo plugin : request.plugins)
		{
			String id = plugin.id;

			Stream<RestPluginHistoryEntry> stream;
			String fromVer = Objects.requireNonNull(plugin.fromVersion, "fromVersion");

			if(Objects.equals(plugin.fromVersion, plugin.toVersion))
			{
				stream = myPluginHistoryService.listPluginHistory(id, plugin.fromVersion);
			}
			else
			{
				stream = myPluginHistoryService.listPluginHistoryByRange(id, fromVer, Objects.requireNonNull(plugin.toVersion), plugin.includeFromVersion);
			}

			stream.forEachOrdered(entry ->
			{
				PluginHistoryResponse.PluginHistory pluginHistory = new PluginHistoryResponse.PluginHistory();
				pluginHistory.id = id;
				pluginHistory.history = entry;

				response.entries.add(pluginHistory);
			});
		}

		return response;
	}
}
