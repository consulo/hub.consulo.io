package consulo.hub.backend.repository.external.pacman;

import consulo.hub.backend.repository.PluginStatisticsService;
import consulo.hub.backend.repository.RepositoryChannelsService;
import consulo.hub.backend.repository.external.AbstractDistributionRepository;
import consulo.hub.backend.repository.external.PackageRepositoryUtil;
import consulo.hub.backend.repository.external.VelocityRenderer;
import consulo.hub.shared.repository.PluginChannel;
import consulo.hub.shared.repository.PluginNode;
import jakarta.annotation.Nonnull;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

/**
 * Builds and caches a Pacman package database (Arch Linux) per channel.
 *
 * @author VISTALL
 */
@Service
public class PacmanDistributionRepository extends AbstractDistributionRepository<byte[]> {

    private final RepositoryChannelsService myChannelsService;
    private final PluginStatisticsService myStatsService;
    private final VelocityRenderer myVelocity;

    @Autowired
    public PacmanDistributionRepository(@Nonnull RepositoryChannelsService repositoryChannelsService,
                                        @Nonnull PluginStatisticsService pluginStatisticsService,
                                        @Nonnull VelocityRenderer velocityRenderer) {
        myChannelsService = repositoryChannelsService;
        myStatsService = pluginStatisticsService;
        myVelocity = velocityRenderer;
    }

    @Override
    @Nonnull
    protected byte[] buildIndex(@Nonnull PluginChannel channel) throws IOException {
        List<PluginNode> plugins = PackageRepositoryUtil.getLatestPlugins(myChannelsService, myStatsService, channel);
        List<PluginNode> platforms = PackageRepositoryUtil.getLinuxPlatformNodes(myChannelsService, myStatsService, channel);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (TarArchiveOutputStream tar = new TarArchiveOutputStream(new GZIPOutputStream(baos))) {
            tar.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);

            for (PluginNode node : plugins) {
                String pkgName = "consulo-plugin-" + node.id.toLowerCase();
                writeDescEntry(tar, pkgName + "-" + node.version + "-1/", renderPluginDesc(node, pkgName));
            }

            for (PluginNode node : platforms) {
                PackageRepositoryUtil.LinuxPlatformInfo info = PackageRepositoryUtil.LINUX_PLATFORMS.get(node.id);
                if (info == null) continue;
                String dirName = info.pkgName() + "-" + node.version + "-1/";
                writeDescEntry(tar, dirName, renderPlatformDesc(node, info));
            }
        }
        return baos.toByteArray();
    }

    private void writeDescEntry(@Nonnull TarArchiveOutputStream tar,
                                 @Nonnull String dirName,
                                 @Nonnull String desc) throws IOException {
        TarArchiveEntry dir = new TarArchiveEntry(dirName);
        tar.putArchiveEntry(dir);
        tar.closeArchiveEntry();

        byte[] descBytes = desc.getBytes(StandardCharsets.UTF_8);
        TarArchiveEntry descEntry = new TarArchiveEntry(dirName + "desc");
        descEntry.setSize(descBytes.length);
        tar.putArchiveEntry(descEntry);
        tar.write(descBytes);
        tar.closeArchiveEntry();
    }

    private String renderPluginDesc(@Nonnull PluginNode node, @Nonnull String pkgName) {
        List<String> deps = new ArrayList<>();
        deps.add("consulo");
        if (node.dependencies != null) {
            for (String dep : node.dependencies) {
                deps.add("consulo-plugin-" + dep.toLowerCase());
            }
        }
        Map<String, Object> ctx = new HashMap<>();
        ctx.put("filename", pkgName + "-" + node.version + "-1-any.pkg.tar.gz");
        ctx.put("name", pkgName);
        ctx.put("version", node.version);
        ctx.put("desc", node.description != null && !node.description.isBlank()
            ? node.description.trim().lines().findFirst().orElse(node.id)
            : (node.name != null ? node.name : node.id));
        ctx.put("size", node.length != null ? node.length : 0);
        ctx.put("md5", PackageRepositoryUtil.checksumLower(node, "md5"));
        ctx.put("sha256", PackageRepositoryUtil.checksumLower(node, "sha256"));
        ctx.put("url", PackageRepositoryUtil.emptyToNull(node.url));
        ctx.put("buildDate", node.date != null ? node.date / 1000 : 0);
        ctx.put("arch", "any");
        ctx.put("provides", null);
        ctx.put("deps", deps);
        return myVelocity.render("templates/pkg/pacman-desc.vm", ctx);
    }

    private String renderPlatformDesc(@Nonnull PluginNode node,
                                       @Nonnull PackageRepositoryUtil.LinuxPlatformInfo info) {
        boolean withJdk = info.pkgName().equals("consulo-with-jdk");
        String desc = withJdk ? "Consulo IDE with bundled JDK"
                              : "Consulo IDE without bundled JDK (requires system Java 21+)";
        String filename = info.pkgName() + "-" + node.version + "-1-" + info.pacmanArch() + ".pkg.tar.gz";

        Map<String, Object> ctx = new HashMap<>();
        ctx.put("filename", filename);
        ctx.put("name", info.pkgName());
        ctx.put("version", node.version);
        ctx.put("desc", desc);
        ctx.put("size", node.length != null ? node.length : 0);
        ctx.put("md5", PackageRepositoryUtil.checksumLower(node, "md5"));
        ctx.put("sha256", PackageRepositoryUtil.checksumLower(node, "sha256"));
        ctx.put("url", null);
        ctx.put("buildDate", node.date != null ? node.date / 1000 : 0);
        ctx.put("arch", info.pacmanArch());
        ctx.put("provides", "consulo");
        ctx.put("deps", Collections.emptyList());
        return myVelocity.render("templates/pkg/pacman-desc.vm", ctx);
    }
}
