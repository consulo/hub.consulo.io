package consulo.hub.backend.repository.impl;

import consulo.hub.backend.repository.*;
import consulo.hub.backend.repository.repository.PluginHistoryEntryRepository;
import consulo.hub.shared.auth.SecurityUtil;
import consulo.hub.shared.auth.domain.UserAccount;
import consulo.hub.shared.repository.PluginChannel;
import consulo.hub.shared.repository.PluginHistoryEntry;
import consulo.hub.shared.repository.PluginNode;
import consulo.util.collection.ArrayUtil;
import consulo.util.lang.VersionComparatorUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author VISTALL
 * @since 05/11/2021
 */
@Service
public class JpaPluginHistoryServiceImpl implements PluginHistoryService
{
	private final PluginHistoryEntryRepository myPluginHistoryEntryRepository;
	private final RepositoryChannelsService myPluginChannelsService;

	@Autowired
	public JpaPluginHistoryServiceImpl(PluginHistoryEntryRepository pluginHistoryEntryRepository, RepositoryChannelsService pluginChannelsService)
	{
		myPluginHistoryEntryRepository = pluginHistoryEntryRepository;
		myPluginChannelsService = pluginChannelsService;
	}

	@Override
	public Stream<RestPluginHistoryEntry> listPluginHistory(String pluginId, String pluginVersion)
	{
		List<PluginHistoryEntry> entryList = myPluginHistoryEntryRepository.findFirst100ByPluginIdAndPluginVersion(pluginId, pluginVersion);
		return entryList.stream().map(this::map);
	}

	@Override
	public Stream<RestPluginHistoryEntry> listPluginHistoryByRange(String pluginId,
																   String fromVer,
																   String toVer,
																   boolean includeFromVersion)
	{
		Set<String> allVersions = new TreeSet<>(VersionComparatorUtil::compare);

		for(PluginChannel pluginChannel : PluginChannel.values())
		{
			RepositoryChannelStore repositoryChannelStore = myPluginChannelsService.getRepositoryByChannel(pluginChannel);
			if(repositoryChannelStore.isLoading())
			{
				continue;
			}

			RepositoryNodeState state = repositoryChannelStore.getState(pluginId);
			if(state == null)
			{
				continue;
			}

			state.forEach(pluginNode -> allVersions.add(pluginNode.version));
		}

		// unknown version
		if(!allVersions.contains(toVer))
		{
			return Stream.of();
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
		return entryList.stream().map(this::map);
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

	@Override
	public void insert(RestPluginHistoryEntry[] historyEntries, PluginNode pluginNode)
	{
		List<PluginHistoryEntry> list = myPluginHistoryEntryRepository.findAllByPluginIdAndPluginVersion(pluginNode.id, pluginNode.version);
		if(!list.isEmpty())
		{
			throw new IllegalArgumentException("history already set");
		}

		UserAccount deployAccount = SecurityUtil.getUserAccout();

		List<PluginHistoryEntry> history = Arrays.stream(historyEntries).map(
				(it) -> {
					PluginHistoryEntry entry = new PluginHistoryEntry();
					// copy history
					entry.setCommitAuthor(it.getCommitAuthor());
					entry.setCommitHash(it.getCommitHash());
					entry.setCommitMessage(it.getCommitMessage());
					entry.setCommitTimestamp(it.getCommitTimestamp());
					entry.setRepoUrl(it.getRepoUrl());

					entry.setPluginId(pluginNode.id);
					entry.setPluginVersion(pluginNode.version);
					entry.setDeployUser(deployAccount);
					return entry;
				}
		).collect(Collectors.toList());

		myPluginHistoryEntryRepository.saveAll(history);
	}
}
