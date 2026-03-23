package consulo.hub.backend.repository.external.apt;

import consulo.hub.backend.repository.PluginStatisticsService;
import consulo.hub.backend.repository.RepositoryChannelsService;
import consulo.hub.backend.repository.external.PackageRepositoryUtil;
import consulo.hub.backend.repository.external.PluginPackageStore;
import consulo.hub.shared.repository.PluginChannel;
import consulo.hub.shared.repository.PluginNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Serves an APT (Debian/Ubuntu) package repository for Consulo plugins and platform packages.
 * <p>
 * Usage in /etc/apt/sources.list:
 * <pre>deb [trusted=yes] https://api.consulo.io/apt release main</pre>
 *
 * @author VISTALL
 */
@RestController
@RequestMapping("/api/apt")
public class AptRepositoryController {

    private final RepositoryChannelsService myRepositoryChannelsService;
    private final PluginStatisticsService myPluginStatisticsService;
    private final AptDistributionRepository myAptRepository;
    private final PluginPackageStore myPackageStore;

    @Autowired
    public AptRepositoryController(RepositoryChannelsService repositoryChannelsService,
                                   PluginStatisticsService pluginStatisticsService,
                                   AptDistributionRepository aptRepository,
                                   PluginPackageStore pluginPackageStore) {
        myRepositoryChannelsService = repositoryChannelsService;
        myPluginStatisticsService = pluginStatisticsService;
        myAptRepository = aptRepository;
        myPackageStore = pluginPackageStore;
    }

    @GetMapping(value = "/dists/{channel}/Release", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> release(@PathVariable PluginChannel channel) {
        AptDistributionRepository.AptIndex index = myAptRepository.getIndex(channel);
        if (index == null) return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        return ResponseEntity.ok(index.release());
    }

    @GetMapping(value = "/dists/{channel}/main/binary-all/Packages", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<byte[]> packages(@PathVariable PluginChannel channel) {
        AptDistributionRepository.AptIndex index = myAptRepository.getIndex(channel);
        if (index == null) return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(index.packagesAll());
    }

    @GetMapping("/dists/{channel}/main/binary-all/Packages.gz")
    public ResponseEntity<byte[]> packagesGz(@PathVariable PluginChannel channel) {
        AptDistributionRepository.AptIndex index = myAptRepository.getIndex(channel);
        if (index == null) return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        return ResponseEntity.ok().header("Content-Type", "application/gzip").body(index.packagesAllGz());
    }

    @GetMapping(value = "/dists/{channel}/main/binary-{arch}/Packages", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<byte[]> packagesArch(@PathVariable PluginChannel channel, @PathVariable String arch) {
        AptDistributionRepository.AptIndex index = myAptRepository.getIndex(channel);
        if (index == null) return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        byte[] data = index.packagesArch().get(arch);
        if (data == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(data);
    }

    @GetMapping("/dists/{channel}/main/binary-{arch}/Packages.gz")
    public ResponseEntity<byte[]> packagesArchGz(@PathVariable PluginChannel channel, @PathVariable String arch) {
        AptDistributionRepository.AptIndex index = myAptRepository.getIndex(channel);
        if (index == null) return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        byte[] data = index.packagesArchGz().get(arch);
        if (data == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok().header("Content-Type", "application/gzip").body(data);
    }

    @GetMapping("/pool/{channel}/{filename:.+}")
    public ResponseEntity<byte[]> download(@PathVariable PluginChannel channel, @PathVariable String filename) {
        if (!filename.endsWith(".deb")) return ResponseEntity.notFound().build();

        String stem = filename.substring(0, filename.length() - ".deb".length());
        int sep = stem.lastIndexOf('_');
        if (sep < 0) return ResponseEntity.notFound().build();
        String nameAndVersion = stem.substring(0, sep);
        int sep2 = nameAndVersion.lastIndexOf('_');
        if (sep2 < 0) return ResponseEntity.notFound().build();
        String pkgName = nameAndVersion.substring(0, sep2);
        String version = nameAndVersion.substring(sep2 + 1);

        if (pkgName.startsWith("consulo-plugin-")) {
            String pluginId = pkgName.substring("consulo-plugin-".length());
            PluginNode node = PackageRepositoryUtil.findPlugin(myRepositoryChannelsService, channel, pluginId, version);
            if (node == null) return ResponseEntity.notFound().build();

            byte[] deb = myPackageStore.getDeb(node);
            if (deb != null) {
                return ResponseEntity.ok()
                    .header("Content-Type", "application/vnd.debian.binary-package")
                    .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                    .body(deb);
            }

            String url = PackageRepositoryUtil.resolveDownloadUrl(channel, node);
            if (url == null) return ResponseEntity.notFound().build();
            return ResponseEntity.status(HttpStatus.FOUND).header("Location", url).build();
        }

        if (pkgName.equals("consulo-with-jdk") || pkgName.equals("consulo-without-jdk")) {
            PluginNode node = findPlatformNode(channel, pkgName, version);
            if (node == null) return ResponseEntity.notFound().build();

            byte[] deb = myPackageStore.getPlatformDeb(node);
            if (deb != null) {
                return ResponseEntity.ok()
                    .header("Content-Type", "application/vnd.debian.binary-package")
                    .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                    .body(deb);
            }

            String url = PackageRepositoryUtil.resolveDownloadUrl(channel, node);
            if (url == null) return ResponseEntity.notFound().build();
            return ResponseEntity.status(HttpStatus.FOUND).header("Location", url).build();
        }

        return ResponseEntity.notFound().build();
    }

    private PluginNode findPlatformNode(PluginChannel channel, String aptPkgName, String version) {
        List<PluginNode> platforms = PackageRepositoryUtil.getLinuxPlatformNodes(
            myRepositoryChannelsService, myPluginStatisticsService, channel);
        for (PluginNode node : platforms) {
            String[] apt = AptDistributionRepository.LINUX_PLATFORM_TO_APT.get(node.id);
            if (apt != null && apt[0].equals(aptPkgName) && node.version.equals(version)) {
                return node;
            }
        }
        return null;
    }
}
