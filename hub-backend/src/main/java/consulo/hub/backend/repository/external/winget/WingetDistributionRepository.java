package consulo.hub.backend.repository.external.winget;

import consulo.hub.backend.repository.PluginStatisticsService;
import consulo.hub.backend.repository.RepositoryChannelsService;
import consulo.hub.backend.repository.external.AbstractDistributionRepository;
import consulo.hub.backend.repository.external.PackageRepositoryUtil;
import consulo.hub.shared.repository.PluginChannel;
import consulo.hub.shared.repository.PluginNode;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Builds and caches a WinGet REST source index per channel.
 *
 * @author VISTALL
 */
@Service
public class WingetDistributionRepository extends AbstractDistributionRepository<WingetDistributionRepository.WingetIndex> {

    public record WingetIndex(
        List<Map<String, Object>> searchEntries,
        Map<String, Map<String, Object>> manifests) {
    }

    private final RepositoryChannelsService myChannelsService;
    private final PluginStatisticsService myStatsService;

    @Autowired
    public WingetDistributionRepository(@Nonnull RepositoryChannelsService repositoryChannelsService,
                                        @Nonnull PluginStatisticsService pluginStatisticsService) {
        myChannelsService = repositoryChannelsService;
        myStatsService = pluginStatisticsService;
    }

    @Override
    @Nonnull
    protected WingetIndex buildIndex(@Nonnull PluginChannel channel) {
        List<PluginNode> plugins = PackageRepositoryUtil.getLatestPlugins(myChannelsService, myStatsService, channel);
        List<PluginNode> platforms = PackageRepositoryUtil.getWindowsPlatformNodes(myChannelsService, myStatsService, channel);

        List<Map<String, Object>> searchEntries = new ArrayList<>();
        Map<String, Map<String, Object>> manifests = new LinkedHashMap<>();

        for (PluginNode node : plugins) {
            String identifier = "consulo.plugin-" + node.id.toLowerCase();
            String name = node.name != null ? node.name : node.id;
            searchEntries.add(WingetRepositoryController.searchEntry(identifier, name, node.version));
            manifests.put(identifier.toLowerCase(), buildPluginVersionEntry(channel, node, identifier));
        }

        Set<String> addedWingetIds = new HashSet<>();
        for (Map.Entry<String, String[]> e : WingetRepositoryController.WINDOWS_PLATFORM_TO_WINGET.entrySet()) {
            String wingetId = e.getValue()[0];
            if (!addedWingetIds.add(wingetId)) continue;
            PluginNode node = findById(platforms, e.getKey());
            if (node == null) continue;
            String name = WingetRepositoryController.platformName(wingetId);
            searchEntries.add(WingetRepositoryController.searchEntry(wingetId, name, node.version));
        }

        Map<String, List<Map<String, Object>>> platformInstallers = new LinkedHashMap<>();
        Map<String, String> platformVersions = new LinkedHashMap<>();
        for (Map.Entry<String, String[]> e : WingetRepositoryController.WINDOWS_PLATFORM_TO_WINGET.entrySet()) {
            String wingetId = e.getValue()[0];
            PluginNode node = findById(platforms, e.getKey());
            if (node == null) continue;
            String url = PackageRepositoryUtil.resolveDownloadUrl(channel, node);
            if (url == null) continue;
            platformVersions.putIfAbsent(wingetId, node.version);

            Map<String, Object> installer = new LinkedHashMap<>();
            installer.put("Architecture", e.getValue()[1]);
            installer.put("InstallerType", e.getValue()[2]);
            installer.put("InstallerUrl", url);
            if (node.checksum != null && node.checksum.sha_256 != null) {
                installer.put("InstallerSha256", node.checksum.sha_256.toLowerCase());
            }
            platformInstallers.computeIfAbsent(wingetId, k -> new ArrayList<>()).add(installer);
        }
        for (Map.Entry<String, List<Map<String, Object>>> e : platformInstallers.entrySet()) {
            String wingetId = e.getKey();
            String desc = wingetId.equals("consulo.with-jdk")
                ? "Consulo IDE with bundled JDK"
                : "Consulo IDE without bundled JDK (requires system Java 21+)";
            manifests.put(wingetId.toLowerCase(),
                WingetRepositoryController.versionEntry(platformVersions.get(wingetId),
                    WingetRepositoryController.platformName(wingetId), desc, e.getValue()));
        }

        return new WingetIndex(Collections.unmodifiableList(searchEntries),
            Collections.unmodifiableMap(manifests));
    }

    private static Map<String, Object> buildPluginVersionEntry(@Nonnull PluginChannel channel,
                                                                @Nonnull PluginNode node,
                                                                @Nonnull String identifier) {
        String url = PackageRepositoryUtil.resolveDownloadUrl(channel, node);
        Map<String, Object> installer = new LinkedHashMap<>();
        installer.put("Architecture", "neutral");
        installer.put("InstallerType", "zip");
        if (url != null) installer.put("InstallerUrl", url);
        if (node.checksum != null && node.checksum.sha_256 != null) {
            installer.put("InstallerSha256", node.checksum.sha_256.toLowerCase());
        }
        String desc = WingetRepositoryController.shortDesc(node);
        String name = node.name != null ? node.name : identifier.substring("consulo.plugin-".length());
        return WingetRepositoryController.versionEntry(node.version, name, desc, List.of(installer));
    }

    @Nullable
    private static PluginNode findById(@Nonnull List<PluginNode> nodes, @Nonnull String id) {
        return nodes.stream().filter(n -> n.id.equals(id)).findFirst().orElse(null);
    }
}
