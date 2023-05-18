package consulo.hub.backend.repository.impl.store;

import com.google.common.annotations.VisibleForTesting;
import consulo.hub.backend.repository.PluginStatisticsService;
import consulo.hub.backend.repository.RepositoryChannelStore;
import consulo.hub.shared.repository.PluginChannel;
import consulo.hub.shared.repository.PluginNode;
import consulo.util.lang.function.ThrowableConsumer;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 18/05/2023
 */
public abstract class BaseRepositoryChannelStore<S extends BaseRepositoryNodeState> implements RepositoryChannelStore
{
	protected final PluginChannel myChannel;

	protected final Map<String, S> myPlugins = new ConcurrentSkipListMap<>();

	protected boolean myLoading;

	public BaseRepositoryChannelStore(PluginChannel channel)
	{
		myChannel = channel;
	}

	@Override
	@Nullable
	public PluginNode select(@Nonnull String platformVersion, @Nonnull String pluginId, @Nullable String version, boolean platformBuildSelect)
	{
		S state = myPlugins.get(pluginId);
		if(state == null)
		{
			return null;
		}

		return state.select(platformVersion, version, platformBuildSelect);
	}

	@Override
	@Nonnull
	public PluginNode[] select(@Nonnull PluginStatisticsService statisticsService, @Nonnull String platformVersion, boolean platformBuildSelect)
	{
		List<PluginNode> list = new ArrayList<>();
		for(S state : myPlugins.values())
		{
			state.selectInto(statisticsService, myChannel, platformVersion, platformBuildSelect, list);
		}
		return list.isEmpty() ? PluginNode.EMPTY_ARRAY : list.toArray(new PluginNode[list.size()]);
	}

	@Override
	public void push(PluginNode pluginNode, String ext, ThrowableConsumer<Path, Exception> writeConsumer) throws Exception
	{
		S pluginsState = myPlugins.computeIfAbsent(pluginNode.id, this::creatRepositoryNodeState);

		pluginsState.push(pluginNode, ext, writeConsumer);
	}

	@VisibleForTesting
	public void _add(PluginNode node) throws Exception
	{
		S pluginsState = myPlugins.computeIfAbsent(node.id, this::creatRepositoryNodeState);

		pluginsState._add(node);
	}

	protected abstract S creatRepositoryNodeState(String pluginId);

	@Override
	public void iteratePluginNodes(@Nonnull Consumer<PluginNode> consumer)
	{
		for(S pluginsState : myPlugins.values())
		{
			pluginsState.forEach(consumer);
		}
	}

	@Override
	@Nullable
	public S getState(String pluginId)
	{
		return myPlugins.get(pluginId);
	}

	@Override
	public void remove(String pluginId, String version, String platformVersion)
	{
		S state = myPlugins.get(pluginId);
		if(state == null)
		{
			return;
		}

		state.remove(version, platformVersion);
	}

	@Override
	public boolean isLoading()
	{
		return myLoading;
	}
}
