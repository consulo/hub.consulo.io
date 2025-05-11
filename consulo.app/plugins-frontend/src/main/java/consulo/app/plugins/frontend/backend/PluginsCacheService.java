package consulo.app.plugins.frontend.backend;

import consulo.app.plugins.frontend.backend.service.BackendRepositoryService;
import consulo.hub.shared.repository.PluginNode;
import consulo.hub.shared.repository.util.RepositoryUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author VISTALL
 * @since 2025-05-11
 */
@Service
public class PluginsCacheService {
    private volatile PluginsCache myPluginsCache;

    private final BackendRepositoryService myBackendRepositoryService;

    @Autowired
    public PluginsCacheService(BackendRepositoryService backendRepositoryService) {
        myBackendRepositoryService = backendRepositoryService;
    }

    @PostConstruct
    public void init() {
        myPluginsCache = loadIfSuccess();
    }

    @Scheduled(timeUnit = TimeUnit.MINUTES, fixedDelay = 1)
    public void refresh() {
        myPluginsCache = loadIfSuccess();
    }

    @Nonnull
    public PluginsCache getPluginsCache() {
        return Objects.requireNonNull(myPluginsCache);
    }

    private PluginsCache loadIfSuccess() {
        while (true) {
            PluginsCache cache = load();
            if (cache.isValid()) {
                return cache;
            }

            try {
                Thread.sleep(1000L);
            }
            catch (InterruptedException ignored) {
            }
        }
    }

    private PluginsCache load() {
        PluginNode[] pluginNodes = myBackendRepositoryService.listOldPlugins();

        List<PluginNode> sortByDownloads = new ArrayList<>(List.of(pluginNodes));
        sortByDownloads.removeIf(node -> RepositoryUtil.isPlatformNode(node.id));
        sortByDownloads.sort((o1, o2) -> Integer.compareUnsigned(o2.downloads, o1.downloads));

        return new PluginsCache(sortByDownloads);
    }
}
