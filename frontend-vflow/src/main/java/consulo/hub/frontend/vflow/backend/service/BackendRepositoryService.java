package consulo.hub.frontend.vflow.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import consulo.hub.frontend.vflow.backend.BackendRequestor;
import consulo.hub.shared.repository.PluginChannel;
import consulo.hub.shared.repository.PluginNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 21/08/2021
 */
@Service
public class BackendRepositoryService
{
	private static final Logger LOG = LoggerFactory.getLogger(BackendRepositoryService.class);

	@Autowired
	private BackendRequestor myBackendRequestor;

	public void listAll(PluginChannel pluginChannel, @Nonnull Consumer<PluginNode> consumer)
	{
		try
		{
			PluginNode[] nodes = myBackendRequestor.runRequest("/repository/list", Map.of("channel", pluginChannel.name()), PluginNode[].class);
			if(nodes == null)
			{
				nodes = new PluginNode[0];
			}

			for(PluginNode node : nodes)
			{
				consumer.accept(node);
			}
		}
		catch(Exception e)
		{
			LOG.error("Fail to get plugins, channel: " + pluginChannel, e);
		}
	}

	public void iteratePlugins(@Nonnull PluginChannel from, @Nonnull PluginChannel to)
	{
		try
		{
			myBackendRequestor.runRequest("/repository/iterate", Map.of("from", from.name(), "to", to.name()), new TypeReference<Map<String, String>>()
			{
			});
		}
		catch(Exception e)
		{
			LOG.error("Fail iterate plugins, from=" + from + " to=" + to, e);
		}
	}
}