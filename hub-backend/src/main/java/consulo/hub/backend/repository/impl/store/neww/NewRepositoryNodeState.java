package consulo.hub.backend.repository.impl.store.neww;

import consulo.hub.backend.repository.impl.store.BaseRepositoryNodeState;
import consulo.hub.backend.util.AccessToken;
import consulo.hub.shared.repository.PluginChannel;
import consulo.hub.shared.repository.PluginNode;
import consulo.util.lang.function.ThrowableConsumer;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author VISTALL
 * @since 18/05/2023
 */
public class NewRepositoryNodeState extends BaseRepositoryNodeState
{
	private final NewInlineRepositoryStore myInlineRepositoryStore;
	private final PluginChannel myPluginChannel;

	public NewRepositoryNodeState(PluginChannel channel, String pluginId, NewInlineRepositoryStore inlineRepositoryStore)
	{
		super(pluginId);
		myPluginChannel = channel;
		myInlineRepositoryStore = inlineRepositoryStore;
	}

	@Override
	public void push(PluginNode pluginNode, String ext, ThrowableConsumer<Path, Exception> writeConsumer) throws Exception
	{
		try (AccessToken ignored = writeLock())
		{
			Path artifactPath = myInlineRepositoryStore.prepareArtifactPath(pluginNode.id, pluginNode.version, ext);

			writeConsumer.consume(artifactPath);

			prepareNode(pluginNode, artifactPath);

			pluginNode.length = Files.size(artifactPath);
			pluginNode.targetPath = artifactPath;
			pluginNode.cleanUp();

			_add(pluginNode);

			myInlineRepositoryStore.updateMeta(pluginNode.id, pluginNode.version, ext, meta ->
			{
				meta.node = pluginNode;
				meta.channels.add(myPluginChannel);
			});
		}
	}

	@Override
	protected void removeRepositoryArtifact(PluginNode target)
	{
		throw new UnsupportedOperationException();
	}
}
