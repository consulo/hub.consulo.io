package consulo.hub.backend.repository.impl.store.neww;

import consulo.hub.backend.repository.impl.store.BaseRepositoryChannelStore;
import consulo.hub.shared.repository.PluginChannel;

/**
 * @author VISTALL
 * @since 18/05/2023
 */
public class NewRepositoryChannelStore extends BaseRepositoryChannelStore<NewRepositoryNodeState>
{
	private final NewInlineRepositoryStore myInlineRepositoryStore;

	public NewRepositoryChannelStore(PluginChannel channel, NewInlineRepositoryStore inlineRepositoryStore)
	{
		super(channel);
		myInlineRepositoryStore = inlineRepositoryStore;
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
