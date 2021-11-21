package consulo.hub.backend.repository;

import com.intellij.util.text.VersionComparatorUtil;
import consulo.hub.backend.repository.pluginsState.PluginsState;
import consulo.hub.backend.repository.repository.PluginHistoryEntryRepository;
import consulo.hub.shared.repository.PluginChannel;
import consulo.hub.shared.repository.PluginHistoryEntry;
import consulo.util.collection.ArrayUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * @author VISTALL
 * @since 05/11/2021
 */
@RestController
public class PluginHistoryRestController
{
	private final PluginHistoryEntryRepository myPluginHistoryEntryRepository;

	private final PluginChannelsService myPluginChannelsService;

	@Autowired
	public PluginHistoryRestController(PluginHistoryEntryRepository pluginHistoryEntryRepository, PluginChannelsService pluginChannelsService)
	{
		myPluginHistoryEntryRepository = pluginHistoryEntryRepository;
		myPluginChannelsService = pluginChannelsService;
	}

	@RequestMapping("/api/repository/history/listByVersion")
	public List<RestPluginHistoryEntry> listPluginHistory(@RequestParam("id") String pluginId, @RequestParam("version") String pluginVersion)
	{
		List<PluginHistoryEntry> entryList = myPluginHistoryEntryRepository.findAllByPluginIdAndPluginVersion(pluginId, pluginVersion);
		return entryList.stream().map(this::map).limit(100).collect(Collectors.toList());
	}

	@RequestMapping("/api/repository/history/listByVersionRange")
	public List<RestPluginHistoryEntry> lustPluginHistoryByRange(@RequestParam("id") String pluginId,
																 @RequestParam("fromVersion") String fromVer,
																 @RequestParam("toVersion") String toVer,
																 @RequestParam(value = "includeFromVersion", required = false, defaultValue = "true") boolean includeFromVersion)
	{
		Set<String> allVersions = new TreeSet<>(VersionComparatorUtil::compare);

		for(PluginChannel pluginChannel : PluginChannel.values())
		{
			PluginChannelService pluginChannelService = myPluginChannelsService.getRepositoryByChannel(pluginChannel);
			if(pluginChannelService.isLoading())
			{
				continue;
			}

			PluginsState state = pluginChannelService.getState(pluginId);
			if(state == null)
			{
				continue;
			}

			state.forEach(pluginNode -> allVersions.add(pluginNode.version));
		}

		// unknown version
		if(!allVersions.contains(toVer))
		{
			return List.of();
		}

		String[] verArray = allVersions.toArray(String[]::new);

		List<String> targetVersions = new ArrayList<>();

		int indexOfToVersion = ArrayUtil.indexOf(verArray, toVer);

		for(int i = indexOfToVersion; i >= 0; i--)
		{
			String tempVer = verArray[i];

			targetVersions.add(tempVer);

			if(tempVer.equals(fromVer))
			{
				break;
			}
		}

		if(!includeFromVersion)
		{
			targetVersions.remove(fromVer);
		}

		List<PluginHistoryEntry> entryList = myPluginHistoryEntryRepository.findAllByPluginIdAndPluginVersionIn(pluginId, targetVersions);
		return entryList.stream().map(this::map).collect(Collectors.toList());
	}

	private RestPluginHistoryEntry map(PluginHistoryEntry jpaHistory)
	{
		RestPluginHistoryEntry entry = new RestPluginHistoryEntry();
		entry.setCommitAuthor(jpaHistory.getCommitAuthor());
		entry.setCommitHash(jpaHistory.getCommitHash());
		entry.setCommitMessage(jpaHistory.getCommitMessage());
		entry.setCommitTimestamp(jpaHistory.getCommitTimestamp());
		entry.setRepoUrl(jpaHistory.getRepoUrl());

		entry.setPluginVersion(jpaHistory.getPluginVersion());
		return entry;
	}
}
