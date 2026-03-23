package consulo.hub.backend.repository.external.rpm;

import consulo.hub.backend.repository.PluginStatisticsService;
import consulo.hub.backend.repository.RepositoryChannelsService;
import consulo.hub.backend.repository.external.AbstractDistributionRepository;
import consulo.hub.backend.repository.external.PackageRepositoryUtil;
import consulo.hub.backend.repository.external.VelocityRenderer;
import consulo.hub.shared.repository.PluginChannel;
import consulo.hub.shared.repository.PluginNode;
import jakarta.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Builds and caches RPM (Fedora/RHEL/openSUSE) repository index files per channel.
 *
 * @author VISTALL
 */
@Service
public class RpmDistributionRepository extends AbstractDistributionRepository<RpmDistributionRepository.RpmIndex> {

    public record RpmIndex(String repomd, byte[] primaryXmlGz) {
    }

    public record RpmPackageItem(
        String name, String version, long ts, long size,
        String sha256, String summary,
        String description, String url, String href, List<String> deps) {
    }

    private final RepositoryChannelsService myChannelsService;
    private final PluginStatisticsService myStatsService;
    private final VelocityRenderer myVelocity;

    @Autowired
    public RpmDistributionRepository(@Nonnull RepositoryChannelsService repositoryChannelsService,
                                     @Nonnull PluginStatisticsService pluginStatisticsService,
                                     @Nonnull VelocityRenderer velocityRenderer) {
        myChannelsService = repositoryChannelsService;
        myStatsService = pluginStatisticsService;
        myVelocity = velocityRenderer;
    }

    @Override
    @Nonnull
    protected RpmIndex buildIndex(@Nonnull PluginChannel channel) throws IOException {
        List<PluginNode> plugins = PackageRepositoryUtil.getLatestPlugins(myChannelsService, myStatsService, channel);
        long timestamp = PackageRepositoryUtil.getRepoTimestamp(plugins);

        List<RpmPackageItem> items = plugins.stream().map(n -> toItem(channel, n)).toList();
        byte[] primaryXml = myVelocity.render("templates/pkg/rpm-primary.xml.vm",
            Map.of("packages", items)).getBytes(StandardCharsets.UTF_8);
        byte[] primaryXmlGz = PackageRepositoryUtil.gzip(primaryXml);

        String repomd = myVelocity.render("templates/pkg/rpm-repomd.xml.vm", Map.of(
            "timestamp", timestamp,
            "primarySha256", PackageRepositoryUtil.hex(PackageRepositoryUtil.digest("SHA-256", primaryXmlGz)),
            "primaryOpenSha256", PackageRepositoryUtil.hex(PackageRepositoryUtil.digest("SHA-256", primaryXml)),
            "gzSize", primaryXmlGz.length,
            "xmlSize", primaryXml.length
        ));

        return new RpmIndex(repomd, primaryXmlGz);
    }

    @Nonnull
    private static RpmPackageItem toItem(@Nonnull PluginChannel channel, @Nonnull PluginNode node) {
        String pkgName = "consulo-plugin-" + node.id.toLowerCase();
        List<String> deps = new ArrayList<>();
        if (node.dependencies != null) {
            for (String dep : node.dependencies) {
                deps.add(escapeXml("consulo-plugin-" + dep.toLowerCase()));
            }
        }
        return new RpmPackageItem(
            escapeXml(pkgName),
            escapeXml(node.version),
            node.date != null ? node.date / 1000 : 0,
            node.length != null ? node.length : 0,
            PackageRepositoryUtil.checksumLower(node, "sha256"),
            escapeXml(node.name != null ? node.name : node.id),
            node.description != null && !node.description.isBlank() ? escapeXml(node.description.trim()) : null,
            node.url != null && !node.url.isEmpty() ? escapeXml(node.url) : null,
            escapeXml(pkgName + "-" + node.version + "-1.noarch.rpm"),
            deps);
    }

    private static String escapeXml(@Nonnull String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
