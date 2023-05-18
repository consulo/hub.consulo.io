package consulo.hub.backend.repository.impl.store.neww;

import consulo.hub.shared.repository.PluginChannel;
import consulo.hub.shared.repository.PluginNode;

import java.util.HashMap;
import java.util.Map;

/**
 * @author VISTALL
 * @since 18/05/2023
 */
public class ImportPluginJoiner
{
	private Map<ImportPlugin, RepositoryNodeMeta> myNodes = new HashMap<>();

	public void join(PluginNode pluginNode, PluginChannel channel)
	{
		RepositoryNodeMeta meta = myNodes.computeIfAbsent(new ImportPlugin(pluginNode.id, pluginNode.version), importPlugin -> new RepositoryNodeMeta());

		meta.channels.add(channel);

		if(meta.node == null && pluginNode.targetFile != null && pluginNode.targetFile.exists())
		{
			meta.node = pluginNode;
		}
	}

	public Map<ImportPlugin, RepositoryNodeMeta> getNodes()
	{
		return myNodes;
	}
}
