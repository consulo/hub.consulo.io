package consulo.hub.backend.repository.external;

import consulo.hub.backend.repository.RepositoryChannelStore;
import consulo.hub.backend.repository.RepositoryChannelsService;
import consulo.hub.shared.repository.PluginChannel;
import jakarta.annotation.Nonnull;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Orchestrates per-channel index regeneration for all registered {@link DistributionRepository}
 * implementations (APT, RPM, Pacman, WinGet, …).
 *
 * @author VISTALL
 */
@Service
public class RepositoryIndexService {
    private static final Logger LOG = LoggerFactory.getLogger(RepositoryIndexService.class);

    private final RepositoryChannelsService myChannelsService;
    private final TaskExecutor myTaskExecutor;
    private final List<DistributionRepository<?>> myRepositories;

    private final Set<PluginChannel> myInitialGenDone = ConcurrentHashMap.newKeySet();
    private final Map<PluginChannel, Boolean> myPendingRegen = new ConcurrentHashMap<>();

    @Autowired
    public RepositoryIndexService(@Nonnull RepositoryChannelsService repositoryChannelsService,
                                  @Nonnull TaskExecutor taskExecutor,
                                  @Nonnull List<DistributionRepository<?>> repositories) {
        myChannelsService = repositoryChannelsService;
        myTaskExecutor = taskExecutor;
        myRepositories = repositories;
    }

    @PostConstruct
    public void init() {
        for (PluginChannel channel : PluginChannel.values()) {
            RepositoryChannelStore store = myChannelsService.getRepositoryByChannel(channel);
            store.addChangeListener(() -> scheduleRegen(channel));
        }
    }

    @Scheduled(fixedDelay = 5000, initialDelay = 5000)
    public void triggerInitialRegen() {
        if (myInitialGenDone.size() == PluginChannel.values().length) {
            return;
        }
        for (PluginChannel channel : PluginChannel.values()) {
            if (!myInitialGenDone.contains(channel)
                && !PackageRepositoryUtil.isLoading(myChannelsService, channel)) {
                myInitialGenDone.add(channel);
                scheduleRegen(channel);
            }
        }
    }

    private void scheduleRegen(@Nonnull PluginChannel channel) {
        if (myPendingRegen.putIfAbsent(channel, Boolean.TRUE) != null) {
            return;
        }
        myTaskExecutor.execute(() -> {
            try {
                regenChannel(channel);
            }
            finally {
                myPendingRegen.remove(channel);
            }
        });
    }

    private void regenChannel(@Nonnull PluginChannel channel) {
        if (PackageRepositoryUtil.isLoading(myChannelsService, channel)) {
            return;
        }
        for (DistributionRepository<?> repo : myRepositories) {
            try {
                repo.rebuild(channel);
            }
            catch (Exception e) {
                LOG.warn("Failed to regenerate {} index for channel {}", repo.getClass().getSimpleName(), channel, e);
            }
        }
        LOG.debug("Regenerated repository indexes for channel {}", channel);
    }
}
