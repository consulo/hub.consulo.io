package consulo.webservice;

import consulo.hub.backend.repository.PluginHistoryService;
import consulo.hub.backend.repository.RestPluginHistoryEntry;
import consulo.hub.shared.repository.PluginNode;

import java.util.stream.Stream;

/**
 * @author VISTALL
 * @since 05/11/2021
 */
public class EmptyPluginHistoryServiceImpl implements PluginHistoryService
{
	@Override
	public void insert(RestPluginHistoryEntry[] historyEntries, PluginNode pluginNode)
	{

	}

	@Override
	public Stream<RestPluginHistoryEntry> listPluginHistory(String pluginId, String pluginVersion)
	{
		return Stream.of();
	}

	@Override
	public Stream<RestPluginHistoryEntry> listPluginHistoryByRange(String pluginId, String fromVer, String toVer, boolean includeFromVersion)
	{
		return Stream.of();
	}
}
