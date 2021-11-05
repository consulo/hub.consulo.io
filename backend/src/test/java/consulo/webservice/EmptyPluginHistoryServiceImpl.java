package consulo.webservice;

import consulo.hub.backend.repository.PluginHistoryService;
import consulo.hub.backend.repository.RestPluginHistoryEntry;
import consulo.hub.shared.repository.PluginNode;

import java.util.List;

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
}
