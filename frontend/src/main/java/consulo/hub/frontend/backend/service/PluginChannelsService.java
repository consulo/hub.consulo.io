package consulo.hub.frontend.backend.service;

import consulo.hub.frontend.backend.BackendRequestor;
import consulo.hub.shared.repository.PluginChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * @author VISTALL
 * @since 21/08/2021
 */
@Service
public class PluginChannelsService
{
	private Map<PluginChannel, BackendPluginChannelService> myServices = new HashMap<>();

	@Autowired
	private BackendRequestor myBackendRequestor;

	public BackendPluginChannelService getRepositoryByChannel(PluginChannel channel)
	{
		return myServices.computeIfAbsent(channel, (pluginChannel) -> new BackendPluginChannelService(pluginChannel, myBackendRequestor));
	}

	public void iteratePlugins(@Nonnull PluginChannel from, @Nonnull PluginChannel to)
	{
		throw new UnsupportedOperationException();
	}
}
