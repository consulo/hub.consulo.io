package consulo.app.plugins.frontend.backend;

import consulo.app.plugins.frontend.backend.service.BackendRepositoryService;
import consulo.hub.shared.repository.PluginNode;
import consulo.hub.shared.repository.util.RepositoryUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author VISTALL
 * @since 2025-05-11
 */
@Service
public class PluginsCacheService {
    private static final Logger LOG = LoggerFactory.getLogger(PluginsCacheService.class);
    
    private volatile PluginsCache myPluginsCache;

    private final BackendRepositoryService myBackendRepositoryService;

    private boolean myShutdown;

    @Autowired
    public PluginsCacheService(BackendRepositoryService backendRepositoryService) {
        myBackendRepositoryService = backendRepositoryService;
    }

    @PostConstruct
    public void init() {
        myPluginsCache = loadIfSuccess();
    }

    @PreDestroy
    public void shutdown() {
        myShutdown = true;
    }

    @Scheduled(timeUnit = TimeUnit.MINUTES, fixedDelay = 1)
    public void refresh() {
        myPluginsCache = loadIfSuccess();
    }

    @Nonnull
    public PluginsCache getPluginsCache() {
        return Objects.requireNonNull(myPluginsCache);
    }

    @Nonnull
    private PluginsCache loadIfSuccess() {
        while (!myShutdown) {
            PluginsCache cache = load();
            if (cache.isValid()) {
                return cache;
            }

            try {
                Thread.sleep(1000L);

                LOG.info("Retrying load plugins data...");
            }
            catch (InterruptedException ignored) {
            }
        }

        // shutdown case - just return empty
        return new PluginsCache(List.of(), Map.of());
    }

    private PluginsCache load() {
        PluginNode[] pluginNodes = myBackendRepositoryService.listOldPlugins();

        List<PluginNode> sortByDownloads = new ArrayList<>(List.of(pluginNodes));
        sortByDownloads.removeIf(node -> RepositoryUtil.isPlatformNode(node.id));
        sortByDownloads.sort((o1, o2) -> Integer.compareUnsigned(o2.downloads, o1.downloads));

        Map<String, PluginNode> map = new HashMap<>();
        for (PluginNode node : pluginNodes) {
            map.put(node.id, node);
        }

        return new PluginsCache(sortByDownloads, map);
    }
}
