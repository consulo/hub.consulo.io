package consulo.hub.backend.repository;

import consulo.hub.shared.repository.PluginChannel;
import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 18/05/2023
 */
public interface RepositoryChannelsService
{
	@Nonnull
	RepositoryChannelStore getRepositoryByChannel(@Nonnull PluginChannel channel);

	@Nonnull
	default String getDeployPluginExtension()
	{
		return RepositoryChannelStore.PLUGIN_EXTENSION;
	}
}
