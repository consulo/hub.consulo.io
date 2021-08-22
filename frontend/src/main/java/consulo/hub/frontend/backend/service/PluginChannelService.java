package consulo.hub.frontend.backend.service;

import consulo.hub.frontend.backend.BackendRequestor;
import consulo.hub.shared.repository.PluginChannel;
import consulo.hub.shared.repository.PluginNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 21/08/2021
 */
public class PluginChannelService
{
	private static final Logger LOG = LoggerFactory.getLogger(PluginChannelService.class);

	private final PluginChannel myPluginChannel;

	private final BackendRequestor myBackendRequestor;

	public PluginChannelService(PluginChannel pluginChannel, BackendRequestor backendRequestor)
	{
		myPluginChannel = pluginChannel;
		myBackendRequestor = backendRequestor;
	}

	public void iteratePluginNodes(@Nonnull Consumer<PluginNode> consumer)
	{
		try
		{
			PluginNode[] nodes = myBackendRequestor.runRequest("/repository/list", Map.of("channel", myPluginChannel.name()), PluginNode[].class);
			for(PluginNode node : nodes)
			{
				consumer.accept(node);
			}
		}
		catch(Exception e)
		{
			LOG.error("Fail to get plugins, channel: " + myPluginChannel, e);
		}
	}
}
