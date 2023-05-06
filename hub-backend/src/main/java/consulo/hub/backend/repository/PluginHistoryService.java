package consulo.hub.backend.repository;

import consulo.hub.shared.repository.PluginNode;

/**
 * @author VISTALL
 * @since 05/11/2021
 */
public interface PluginHistoryService
{
	void insert(RestPluginHistoryEntry[] historyEntries, PluginNode pluginNode);
}
