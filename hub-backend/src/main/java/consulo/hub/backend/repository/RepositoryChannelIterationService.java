package consulo.hub.backend.repository;

import consulo.hub.shared.repository.PluginChannel;
import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 23/05/2023
 */
public interface RepositoryChannelIterationService
{
	void iterate(@Nonnull PluginChannel from, @Nonnull PluginChannel to);
}
