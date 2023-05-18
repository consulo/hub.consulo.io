package consulo.hub.backend.repository.impl.store.neww;

import consulo.hub.shared.repository.PluginChannel;
import consulo.hub.shared.repository.PluginNode;

import java.util.TreeSet;

/**
 * @author VISTALL
 * @since 18/05/2023
 */
public class RepositoryNodeMeta
{
	public PluginNode node;

	public TreeSet<PluginChannel> channels = new TreeSet<>();
}
