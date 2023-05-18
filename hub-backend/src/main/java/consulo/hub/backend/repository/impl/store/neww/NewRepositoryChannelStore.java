package consulo.hub.backend.repository.impl.store.neww;

import consulo.hub.backend.repository.impl.store.BaseRepositoryChannelStore;
import consulo.hub.shared.repository.PluginChannel;
import consulo.hub.shared.repository.PluginNode;
import consulo.util.collection.ArrayUtil;

import java.io.IOException;

/**
 * @author VISTALL
 * @since 18/05/2023
 */
public class NewRepositoryChannelStore extends BaseRepositoryChannelStore<NewRepositoryNodeState>
{
	private final NewInlineRepositoryStore myInlineRepositoryStore;
	private final NewRepositoryChannelsService myRepositoryChannelsService;

	public NewRepositoryChannelStore(PluginChannel channel, NewInlineRepositoryStore inlineRepositoryStore, NewRepositoryChannelsService repositoryChannelsService)
	{
		super(channel);
		myInlineRepositoryStore = inlineRepositoryStore;
		myRepositoryChannelsService = repositoryChannelsService;
	}

	@Override
	public void attachDownloadUrl(PluginNode pluginNode, String downloadUrl) throws IOException
	{
		String ext = myRepositoryChannelsService.getNodeExtension(pluginNode);

		myInlineRepositoryStore.updateMeta(pluginNode.id, pluginNode.version, ext, meta ->
		{
			PluginNode node = meta.node;
			String[] downloadUrls = node.downloadUrls;
			if(downloadUrls == null)
			{
				node.downloadUrls = new String[]{downloadUrl};
			}
			else
			{
				node.downloadUrls = ArrayUtil.append(node.downloadUrls, downloadUrl);
			}
		});
	}

	@Override
	protected NewRepositoryNodeState creatRepositoryNodeState(String pluginId)
	{
		return new NewRepositoryNodeState(myChannel, pluginId, myInlineRepositoryStore);
	}

	@Override
	public boolean isLoading()
	{
		return myInlineRepositoryStore.isLoading();
	}
}
