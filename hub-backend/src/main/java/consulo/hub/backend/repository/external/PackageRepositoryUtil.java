package consulo.hub.backend.repository.external;

import consulo.hub.backend.repository.PluginStatisticsService;
import consulo.hub.backend.repository.RepositoryChannelStore;
import consulo.hub.backend.repository.RepositoryChannelsService;
import consulo.hub.shared.repository.PluginChannel;
import consulo.hub.shared.repository.PluginNode;
import consulo.hub.shared.repository.util.PlatformNodeDesc;
import consulo.hub.shared.repository.util.RepositoryUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

/**
 * @author VISTALL
 */
public final class PackageRepositoryUtil {

    public record LinuxPlatformInfo(String pkgName, String debArch, String pacmanArch) {
    }

    public static final Map<String, LinuxPlatformInfo> LINUX_PLATFORMS;

    static {
        Map<String, LinuxPlatformInfo> m = new LinkedHashMap<>();
        m.put("consulo.dist.linux.no.jre",  new LinuxPlatformInfo("consulo-without-jdk", "amd64",   "x86_64"));
        m.put("consulo.dist.linux",          new LinuxPlatformInfo("consulo-with-jdk",    "i386",    "i686"));
        m.put("consulo.dist.linux64",        new LinuxPlatformInfo("consulo-with-jdk",    "amd64",   "x86_64"));
        m.put("consulo.dist.linux.aarch64",  new LinuxPlatformInfo("consulo-with-jdk",    "arm64",   "aarch64"));
        m.put("consulo.dist.linux.riscv64",  new LinuxPlatformInfo("consulo-with-jdk",    "riscv64", "riscv64"));
        m.put("consulo.dist.linux.loong64",  new LinuxPlatformInfo("consulo-with-jdk",    "loong64", "loong64"));
        LINUX_PLATFORMS = Collections.unmodifiableMap(m);
    }

    private PackageRepositoryUtil() {
    }

    @Nonnull
    public static List<PluginNode> getLatestPlugins(@Nonnull RepositoryChannelsService service,
                                                    @Nonnull PluginStatisticsService statsService,
                                                    @Nonnull PluginChannel channel) {
        RepositoryChannelStore store = service.getRepositoryByChannel(channel);
        List<PluginNode> all = store.select(statsService, RepositoryChannelStore.SNAPSHOT, false);
        all.removeIf(n -> n.obsolete || RepositoryUtil.isPlatformNode(n.id));
        return all;
    }

    @Nonnull
    public static List<PluginNode> getLinuxPlatformNodes(@Nonnull RepositoryChannelsService service,
                                                         @Nonnull PluginStatisticsService statsService,
                                                         @Nonnull PluginChannel channel) {
        RepositoryChannelStore store = service.getRepositoryByChannel(channel);
        List<PluginNode> all = store.select(statsService, RepositoryChannelStore.SNAPSHOT, false);
        all.removeIf(n -> {
            PlatformNodeDesc desc = PlatformNodeDesc.getNode(n.id);
            return desc == null || !n.id.startsWith("consulo.dist.linux");
        });
        return all;
    }

    @Nonnull
    public static List<PluginNode> getMacPlatformNodes(@Nonnull RepositoryChannelsService service,
                                                       @Nonnull PluginStatisticsService statsService,
                                                       @Nonnull PluginChannel channel) {
        RepositoryChannelStore store = service.getRepositoryByChannel(channel);
        List<PluginNode> all = store.select(statsService, RepositoryChannelStore.SNAPSHOT, false);
        all.removeIf(n -> {
            PlatformNodeDesc desc = PlatformNodeDesc.getNode(n.id);
            return desc == null || !n.id.startsWith("consulo.dist.mac");
        });
        return all;
    }

    @Nonnull
    public static List<PluginNode> getWindowsPlatformNodes(@Nonnull RepositoryChannelsService service,
                                                            @Nonnull PluginStatisticsService statsService,
                                                            @Nonnull PluginChannel channel) {
        RepositoryChannelStore store = service.getRepositoryByChannel(channel);
        List<PluginNode> all = store.select(statsService, RepositoryChannelStore.SNAPSHOT, false);
        all.removeIf(n -> {
            PlatformNodeDesc desc = PlatformNodeDesc.getNode(n.id);
            return desc == null || !n.id.startsWith("consulo.dist.windows");
        });
        return all;
    }

    @Nullable
    public static PluginNode findPlugin(@Nonnull RepositoryChannelsService service,
                                        @Nonnull PluginChannel channel,
                                        @Nonnull String pluginId,
                                        @Nonnull String version) {
        RepositoryChannelStore store = service.getRepositoryByChannel(channel);
        return store.select(RepositoryChannelStore.SNAPSHOT, pluginId, version, false);
    }

    public static boolean isLoading(@Nonnull RepositoryChannelsService service, @Nonnull PluginChannel channel) {
        return service.getRepositoryByChannel(channel).isLoading();
    }

    public static long getRepoTimestamp(@Nonnull List<PluginNode> plugins) {
        return plugins.stream()
            .mapToLong(n -> n.date != null ? n.date / 1000 : 0L)
            .max()
            .orElse(System.currentTimeMillis() / 1000);
    }

    @Nullable
    public static String resolveDownloadUrl(@Nonnull PluginChannel channel, @Nonnull PluginNode node) {
        if (node.downloadUrls != null && node.downloadUrls.length > 0) {
            return node.downloadUrls[0];
        }
        if (node.targetPath == null && node.targetFile == null) {
            return null;
        }
        return "/api/repository/download?channel=" + channel.name()
            + "&platformVersion=" + node.platformVersion
            + "&id=" + node.id
            + "&version=" + node.version
            + "&noTracking=true";
    }

    public static byte[] gzip(byte[] data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPOutputStream gz = new GZIPOutputStream(baos)) {
            gz.write(data);
        }
        return baos.toByteArray();
    }

    public static byte[] digest(@Nonnull String algorithm, byte[] data) {
        try {
            return MessageDigest.getInstance(algorithm).digest(data);
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public static String hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    @Nullable
    public static String checksumLower(@Nonnull PluginNode node, @Nonnull String type) {
        if (node.checksum == null) return null;
        return switch (type) {
            case "md5"    -> node.checksum.md5 != null ? node.checksum.md5.toLowerCase() : null;
            case "sha256" -> node.checksum.sha_256 != null ? node.checksum.sha_256.toLowerCase() : null;
            default       -> null;
        };
    }

    @Nullable
    public static String emptyToNull(@Nullable String s) {
        return s != null && !s.isEmpty() ? s : null;
    }
}
