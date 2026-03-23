package consulo.hub.backend.repository.external.pacman;

import consulo.hub.backend.repository.RepositoryChannelsService;
import consulo.hub.backend.repository.external.PackageRepositoryUtil;
import consulo.hub.backend.repository.external.PluginPackageStore;
import consulo.hub.shared.repository.PluginChannel;
import consulo.hub.shared.repository.PluginNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Serves a Pacman package database for Consulo plugins and platform distributions (Arch Linux).
 * <p>
 * Usage in /etc/pacman.conf:
 * <pre>
 * [consulo-release]
 * Server = https://api.consulo.io/pacman/release
 * SigLevel = Never
 * </pre>
 *
 * @author VISTALL
 */
@RestController
@RequestMapping("/api/pacman")
public class PacmanRepositoryController {

    private final RepositoryChannelsService myRepositoryChannelsService;
    private final PacmanDistributionRepository myPacmanRepository;
    private final PluginPackageStore myPackageStore;

    @Autowired
    public PacmanRepositoryController(RepositoryChannelsService repositoryChannelsService,
                                      PacmanDistributionRepository pacmanRepository,
                                      PluginPackageStore pluginPackageStore) {
        myRepositoryChannelsService = repositoryChannelsService;
        myPacmanRepository = pacmanRepository;
        myPackageStore = pluginPackageStore;
    }

    @GetMapping({"/{channel}/consulo.db", "/{channel}/consulo.db.tar.gz"})
    public ResponseEntity<byte[]> db(@PathVariable PluginChannel channel) {
        byte[] db = myPacmanRepository.getIndex(channel);
        if (db == null) return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        return ResponseEntity.ok().header("Content-Type", "application/x-gtar").body(db);
    }

    /**
     * Plugin filename:   {@code consulo-plugin-{pluginId}-{version}-1-any.pkg.tar.gz}
     * Platform filename: {@code {pkgName}-{version}-1-{arch}.pkg.tar.gz}
     */
    @GetMapping("/{channel}/{filename:.+}")
    public ResponseEntity<byte[]> download(@PathVariable PluginChannel channel, @PathVariable String filename) {
        // Plugin packages end with -1-any.pkg.tar.gz
        if (filename.endsWith("-1-any.pkg.tar.gz")) {
            String stem = filename.substring(0, filename.length() - "-1-any.pkg.tar.gz".length());
            if (stem.startsWith("consulo-plugin-")) {
                int lastDash = stem.lastIndexOf('-');
                if (lastDash >= 0) {
                    String pluginId = stem.substring("consulo-plugin-".length(), lastDash);
                    String version = stem.substring(lastDash + 1);

                    PluginNode node = PackageRepositoryUtil.findPlugin(myRepositoryChannelsService, channel, pluginId, version);
                    if (node == null) return ResponseEntity.notFound().build();

                    byte[] pkg = myPackageStore.getPkg(node);
                    if (pkg != null) {
                        return ResponseEntity.ok()
                            .header("Content-Type", "application/x-tar")
                            .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                            .body(pkg);
                    }

                    String url = PackageRepositoryUtil.resolveDownloadUrl(channel, node);
                    if (url == null) return ResponseEntity.notFound().build();
                    return ResponseEntity.status(HttpStatus.FOUND).header("Location", url).build();
                }
            }
        }

        // Platform packages: {pkgName}-{version}-1-{pacmanArch}.pkg.tar.gz
        if (filename.endsWith(".pkg.tar.gz")) {
            for (Map.Entry<String, PackageRepositoryUtil.LinuxPlatformInfo> e : PackageRepositoryUtil.LINUX_PLATFORMS.entrySet()) {
                PackageRepositoryUtil.LinuxPlatformInfo info = e.getValue();
                String suffix = "-1-" + info.pacmanArch() + ".pkg.tar.gz";
                String prefix = info.pkgName() + "-";
                if (!filename.startsWith(prefix) || !filename.endsWith(suffix)) continue;

                String version = filename.substring(prefix.length(), filename.length() - suffix.length());
                PluginNode node = PackageRepositoryUtil.findPlugin(myRepositoryChannelsService, channel, e.getKey(), version);
                if (node == null) continue;

                byte[] pkg = myPackageStore.getPlatformPkg(node);
                if (pkg != null) {
                    return ResponseEntity.ok()
                        .header("Content-Type", "application/x-tar")
                        .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                        .body(pkg);
                }

                String url = PackageRepositoryUtil.resolveDownloadUrl(channel, node);
                if (url == null) return ResponseEntity.notFound().build();
                return ResponseEntity.status(HttpStatus.FOUND).header("Location", url).build();
            }
        }

        return ResponseEntity.notFound().build();
    }
}
