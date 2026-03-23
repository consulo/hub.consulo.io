package consulo.hub.backend.repository.external.apt;

import consulo.hub.backend.repository.PluginStatisticsService;
import consulo.hub.backend.repository.RepositoryChannelsService;
import consulo.hub.backend.repository.external.AbstractDistributionRepository;
import consulo.hub.backend.repository.external.PackageRepositoryUtil;
import consulo.hub.backend.repository.external.VelocityRenderer;
import consulo.hub.shared.repository.PluginChannel;
import consulo.hub.shared.repository.PluginNode;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Builds and caches APT (Debian/Ubuntu) repository index files per channel.
 *
 * @author VISTALL
 */
@Service
public class AptDistributionRepository extends AbstractDistributionRepository<AptDistributionRepository.AptIndex> {

    public static final Map<String, String[]> LINUX_PLATFORM_TO_APT;

    static {
        Map<String, String[]> m = new LinkedHashMap<>();
        m.put("consulo.dist.linux.no.jre",  new String[]{"consulo-without-jdk", "amd64"});
        m.put("consulo.dist.linux",          new String[]{"consulo-with-jdk",    "i386"});
        m.put("consulo.dist.linux64",        new String[]{"consulo-with-jdk",    "amd64"});
        m.put("consulo.dist.linux.aarch64",  new String[]{"consulo-with-jdk",    "arm64"});
        m.put("consulo.dist.linux.riscv64",  new String[]{"consulo-with-jdk",    "riscv64"});
        m.put("consulo.dist.linux.loong64",  new String[]{"consulo-with-jdk",    "loong64"});
        LINUX_PLATFORM_TO_APT = Collections.unmodifiableMap(m);
    }

    public static final List<String> ALL_ARCHES = List.of("amd64", "arm64", "i386", "riscv64", "loong64");

    public record AptIndex(String release,
                           byte[] packagesAll,
                           byte[] packagesAllGz,
                           Map<String, byte[]> packagesArch,
                           Map<String, byte[]> packagesArchGz) {
    }

    public record ChecksumEntry(String hash, long size, String path) {
    }

    public record AptPackageItem(
        String name, String version, String arch, String maintainer,
        Long installedSize, Long size,
        String provides,
        String depends,
        String filename, String md5, String sha256, String homepage,
        String descSummary, String descBody) {
    }

    private final RepositoryChannelsService myChannelsService;
    private final PluginStatisticsService myStatsService;
    private final VelocityRenderer myVelocity;

    @Autowired
    public AptDistributionRepository(@Nonnull RepositoryChannelsService repositoryChannelsService,
                                     @Nonnull PluginStatisticsService pluginStatisticsService,
                                     @Nonnull VelocityRenderer velocityRenderer) {
        myChannelsService = repositoryChannelsService;
        myStatsService = pluginStatisticsService;
        myVelocity = velocityRenderer;
    }

    @Override
    @Nonnull
    protected AptIndex buildIndex(@Nonnull PluginChannel channel) throws IOException {
        List<PluginNode> plugins = PackageRepositoryUtil.getLatestPlugins(myChannelsService, myStatsService, channel);
        List<PluginNode> platforms = PackageRepositoryUtil.getLinuxPlatformNodes(myChannelsService, myStatsService, channel);
        List<AptPackageItem> pluginItems = plugins.stream().map(n -> toPluginItem(channel, n)).toList();

        byte[] packagesAll = renderPackages(pluginItems);
        byte[] packagesAllGz = PackageRepositoryUtil.gzip(packagesAll);

        Map<String, byte[]> packagesArch = new LinkedHashMap<>();
        Map<String, byte[]> packagesArchGz = new LinkedHashMap<>();
        for (String arch : ALL_ARCHES) {
            byte[] content = renderPackages(buildArchItems(channel, platforms, arch, pluginItems));
            packagesArch.put(arch, content);
            packagesArchGz.put(arch, PackageRepositoryUtil.gzip(content));
        }

        long ts = PackageRepositoryUtil.getRepoTimestamp(plugins);
        String date = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)
            .format(new Date(ts * 1000L));

        String release = myVelocity.render("templates/pkg/apt-release.vm", Map.of(
            "channel", channel.name(),
            "architectures", String.join(" ", ALL_ARCHES) + " all",
            "date", date,
            "md5", buildChecksumEntries("MD5", packagesAll, packagesAllGz, packagesArch, packagesArchGz),
            "sha256", buildChecksumEntries("SHA-256", packagesAll, packagesAllGz, packagesArch, packagesArchGz)
        ));

        return new AptIndex(release, packagesAll, packagesAllGz,
            Collections.unmodifiableMap(packagesArch),
            Collections.unmodifiableMap(packagesArchGz));
    }

    private byte[] renderPackages(@Nonnull List<AptPackageItem> items) {
        return myVelocity.render("templates/pkg/apt-packages.vm", Map.of("packages", items))
            .getBytes(StandardCharsets.UTF_8);
    }

    private static List<AptPackageItem> buildArchItems(@Nonnull PluginChannel channel,
                                                        @Nonnull List<PluginNode> platforms,
                                                        @Nonnull String arch,
                                                        @Nonnull List<AptPackageItem> pluginItems) {
        List<AptPackageItem> items = new ArrayList<>();
        for (PluginNode node : platforms) {
            String[] apt = LINUX_PLATFORM_TO_APT.get(node.id);
            if (apt != null && apt[1].equals(arch)) {
                items.add(toPlatformItem(channel, node, apt[0], arch));
            }
        }
        items.addAll(pluginItems);
        return items;
    }

    private static List<ChecksumEntry> buildChecksumEntries(@Nonnull String algo,
                                                             byte[] packagesAll, byte[] packagesAllGz,
                                                             @Nonnull Map<String, byte[]> packagesArch,
                                                             @Nonnull Map<String, byte[]> packagesArchGz) {
        List<ChecksumEntry> entries = new ArrayList<>();
        entries.add(toChecksumEntry(algo, packagesAll, "main/binary-all/Packages"));
        entries.add(toChecksumEntry(algo, packagesAllGz, "main/binary-all/Packages.gz"));
        for (String arch : ALL_ARCHES) {
            entries.add(toChecksumEntry(algo, packagesArch.get(arch), "main/binary-" + arch + "/Packages"));
            entries.add(toChecksumEntry(algo, packagesArchGz.get(arch), "main/binary-" + arch + "/Packages.gz"));
        }
        return entries;
    }

    private static ChecksumEntry toChecksumEntry(@Nonnull String algo, byte[] data, @Nonnull String path) {
        return new ChecksumEntry(
            PackageRepositoryUtil.hex(PackageRepositoryUtil.digest(algo, data)),
            data.length, path);
    }

    @Nonnull
    private static AptPackageItem toPluginItem(@Nonnull PluginChannel channel, @Nonnull PluginNode node) {
        String pkgName = "consulo-plugin-" + node.id.toLowerCase();
        String maintainer = (node.vendor != null ? node.vendor : "Consulo") + " <noreply@consulo.io>";
        String filename = "pool/" + channel.name() + "/" + pkgName + "_" + node.version + "_all.deb";
        String raw = node.description != null && !node.description.isBlank()
            ? node.description.trim() : (node.name != null ? node.name : node.id);
        String[] parts = raw.split("\r?\n", 2);
        return new AptPackageItem(
            pkgName, node.version, "all", maintainer,
            node.length != null ? node.length / 1024 : null, node.length,
            null, buildPluginDepends(node.dependencies),
            filename,
            PackageRepositoryUtil.checksumLower(node, "md5"),
            PackageRepositoryUtil.checksumLower(node, "sha256"),
            PackageRepositoryUtil.emptyToNull(node.url),
            parts[0].trim(), parts.length > 1 ? descBody(parts[1]) : null);
    }

    @Nonnull
    private static AptPackageItem toPlatformItem(@Nonnull PluginChannel channel, @Nonnull PluginNode node,
                                                  @Nonnull String pkgName, @Nonnull String arch) {
        String filename = "pool/" + channel.name() + "/" + pkgName + "_" + node.version + "_" + arch + ".deb";
        return new AptPackageItem(
            pkgName, node.version, arch, "Consulo <noreply@consulo.io>",
            node.length != null ? node.length / 1024 : null, node.length,
            "consulo", null,
            filename,
            PackageRepositoryUtil.checksumLower(node, "md5"),
            PackageRepositoryUtil.checksumLower(node, "sha256"),
            null,
            node.name != null ? node.name : pkgName, null);
    }

    @Nonnull
    private static String buildPluginDepends(@Nullable String[] dependencies) {
        StringJoiner sj = new StringJoiner(", ");
        sj.add("consulo-with-jdk | consulo-without-jdk");
        if (dependencies != null) {
            for (String dep : dependencies) {
                sj.add("consulo-plugin-" + dep.toLowerCase());
            }
        }
        return sj.toString();
    }

    @Nullable
    private static String descBody(@Nonnull String rest) {
        StringBuilder sb = new StringBuilder();
        for (String line : rest.split("\r?\n")) {
            String t = line.trim();
            sb.append(' ').append(t.isEmpty() ? "." : t).append('\n');
        }
        if (sb.length() > 0) sb.setLength(sb.length() - 1);
        return sb.isEmpty() ? null : sb.toString();
    }
}
