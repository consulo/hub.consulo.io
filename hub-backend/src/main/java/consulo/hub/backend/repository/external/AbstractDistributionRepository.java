package consulo.hub.backend.repository.external;

import consulo.hub.shared.repository.PluginChannel;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Base class for distribution-format repository implementations.
 * Holds a per-channel cache and delegates index generation to {@link #buildIndex(PluginChannel)}.
 *
 * @param <T> the index type produced by this distribution format
 * @author VISTALL
 */
public abstract class AbstractDistributionRepository<T> implements DistributionRepository<T> {

    private final Map<PluginChannel, T> myCache = new ConcurrentHashMap<>();

    @Override
    @Nullable
    public T getIndex(@Nonnull PluginChannel channel) {
        return myCache.get(channel);
    }

    @Override
    public void rebuild(@Nonnull PluginChannel channel) throws Exception {
        myCache.put(channel, buildIndex(channel));
    }

    @Nonnull
    protected abstract T buildIndex(@Nonnull PluginChannel channel) throws Exception;
}
