package consulo.hub.backend.repository.impl;

import consulo.hub.backend.repository.PluginHistoryService;
import consulo.hub.backend.repository.RestPluginHistoryEntry;
import consulo.hub.backend.repository.repository.PluginHistoryEntryRepository;
import consulo.hub.shared.auth.SecurityUtil;
import consulo.hub.shared.auth.domain.UserAccount;
import consulo.hub.shared.repository.PluginHistoryEntry;
import consulo.hub.shared.repository.PluginNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author VISTALL
 * @since 05/11/2021
 */
@Service
public class JpaPluginHistoryServiceImpl implements PluginHistoryService
{
	private final PluginHistoryEntryRepository myPluginHistoryEntryRepository;

	@Autowired
	public JpaPluginHistoryServiceImpl(PluginHistoryEntryRepository pluginHistoryEntryRepository)
	{
		myPluginHistoryEntryRepository = pluginHistoryEntryRepository;
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
