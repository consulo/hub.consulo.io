package consulo.hub.backend.repository.external;

import consulo.hub.backend.repository.PluginStatisticsService;
import consulo.hub.backend.repository.RepositoryChannelStore;
import consulo.hub.backend.repository.RepositoryChannelsService;
import consulo.hub.shared.repository.PluginChannel;
import consulo.hub.shared.repository.PluginNode;
import jakarta.annotation.Nonnull;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Background service that pre-builds native Linux packages (.deb, .pkg.tar.gz)
 * for both plugins and platform distributions, storing results in {@link PluginPackageStore}.
 *
 * @author VISTALL
 */
@Service
public class PackageRebuilderService {
    private static final Logger LOG = LoggerFactory.getLogger(PackageRebuilderService.class);

    private final RepositoryChannelsService myChannelsService;
    private final PluginStatisticsService myStatsService;
    private final PluginPackageStore myStore;
    private final TaskExecutor myTaskExecutor;

    private final Set<PluginChannel> myInitialRepackScheduled = ConcurrentHashMap.newKeySet();
    private final Map<PluginChannel, Boolean> myPendingRepack = new ConcurrentHashMap<>();

    @Autowired
    public PackageRebuilderService(@Nonnull RepositoryChannelsService repositoryChannelsService,
                                   @Nonnull PluginStatisticsService pluginStatisticsService,
                                   @Nonnull PluginPackageStore pluginPackageStore,
                                   @Nonnull TaskExecutor taskExecutor) {
        myChannelsService = repositoryChannelsService;
        myStatsService = pluginStatisticsService;
        myStore = pluginPackageStore;
        myTaskExecutor = taskExecutor;
    }

    @PostConstruct
    public void init() {
        for (PluginChannel channel : PluginChannel.values()) {
            RepositoryChannelStore store = myChannelsService.getRepositoryByChannel(channel);
            store.addChangeListener(() -> scheduleRepackChannel(channel));
        }
    }

    @Scheduled(fixedDelay = 5000, initialDelay = 5000)
    public void triggerInitialRepack() {
        if (myInitialRepackScheduled.size() == PluginChannel.values().length) {
            return;
        }
        for (PluginChannel channel : PluginChannel.values()) {
            if (!myInitialRepackScheduled.contains(channel)
                && !PackageRepositoryUtil.isLoading(myChannelsService, channel)) {
                myInitialRepackScheduled.add(channel);
                scheduleRepackChannel(channel);
            }
        }
    }

    private void scheduleRepackChannel(@Nonnull PluginChannel channel) {
        if (myPendingRepack.putIfAbsent(channel, Boolean.TRUE) != null) {
            return;
        }
        myTaskExecutor.execute(() -> {
            try {
                repackChannel(channel);
            }
            finally {
                myPendingRepack.remove(channel);
            }
        });
    }

    private void repackChannel(@Nonnull PluginChannel channel) {
        if (PackageRepositoryUtil.isLoading(myChannelsService, channel)) {
            return;
        }

        List<PluginNode> plugins = PackageRepositoryUtil.getLatestPlugins(myChannelsService, myStatsService, channel);
        for (PluginNode cloned : plugins) {
            if (myStore.hasPlugin(cloned)) {
                continue;
            }
            PluginNode node = PackageRepositoryUtil.findPlugin(myChannelsService, channel, cloned.id, cloned.version);
            if (node == null) {
                continue;
            }
            buildIfAbsent(myStore.pluginPath(node, "deb"), () -> PluginPackageBuilder.buildDeb(node));
            buildIfAbsent(myStore.pluginPath(node, "pkg.tar.gz"), () -> PluginPackageBuilder.buildPkg(node));
        }

        List<PluginNode> platforms = PackageRepositoryUtil.getLinuxPlatformNodes(myChannelsService, myStatsService, channel);
        for (PluginNode cloned : platforms) {
            PackageRepositoryUtil.LinuxPlatformInfo info = PackageRepositoryUtil.LINUX_PLATFORMS.get(cloned.id);
            if (info == null) {
                continue;
            }
            if (myStore.hasPlatform(cloned)) {
                continue;
            }
            PluginNode node = PackageRepositoryUtil.findPlugin(myChannelsService, channel, cloned.id, cloned.version);
            if (node == null) {
                continue;
            }
            buildIfAbsent(myStore.platformPath(node, "deb"), () -> PluginPackageBuilder.buildPlatformDeb(node, info));
            buildIfAbsent(myStore.platformPath(node, "pkg.tar.gz"), () -> PluginPackageBuilder.buildPlatformPkg(node, info));
        }
    }

    private void buildIfAbsent(@Nonnull Path path, @Nonnull IoSupplier<byte[]> builder) {
        if (Files.exists(path)) {
            return;
        }
        try {
            byte[] data = builder.get();
            if (data == null) {
                return;
            }
            Files.createDirectories(path.getParent());
            Files.write(path, data);
            LOG.debug("Built package: {}", path.getFileName());
        }
        catch (Exception e) {
            LOG.warn("Failed to build package {}: {}", path.getFileName(), e.getMessage());
        }
    }

    @FunctionalInterface
    private interface IoSupplier<T> {
        T get() throws IOException;
    }
}
