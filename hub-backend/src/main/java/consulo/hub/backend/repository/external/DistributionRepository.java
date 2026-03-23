package consulo.hub.backend.repository.external;

import consulo.hub.shared.repository.PluginChannel;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * SPI for components that maintain a per-channel distribution-format index cache.
 * <p>
 * Implementations are auto-discovered via Spring (injected as {@code List<DistributionRepository<?>>})
 * and called by {@link RepositoryIndexService} whenever channel content changes.
 * Controllers inject the concrete typed implementation directly to serve cached indexes.
 *
 * @param <T> the index type produced by this distribution format
 * @author VISTALL
 */
public interface DistributionRepository<T> {
    /**
     * Rebuilds and caches the index for the given channel.
     * Called on a background executor; must be thread-safe.
     */
    void rebuild(@Nonnull PluginChannel channel) throws Exception;

    /**
     * Returns the cached index for the given channel, or {@code null} if not yet generated
     * (e.g. while the channel is still loading from disk on startup).
     */
    @Nullable T getIndex(@Nonnull PluginChannel channel);
}
