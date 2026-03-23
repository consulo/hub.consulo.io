package consulo.hub.backend.repository.external.rpm;

import consulo.hub.backend.repository.RepositoryChannelsService;
import consulo.hub.backend.repository.external.PackageRepositoryUtil;
import consulo.hub.shared.repository.PluginChannel;
import consulo.hub.shared.repository.PluginNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Serves an RPM package repository for Consulo plugins (DNF/YUM/Zypper).
 * <p>
 * Usage in /etc/yum.repos.d/consulo.repo:
 * <pre>
 * [consulo-release]
 * name=Consulo Plugin Repository
 * baseurl=https://api.consulo.io/rpm/release
 * enabled=1
 * gpgcheck=0
 * </pre>
 *
 * @author VISTALL
 */
@RestController
@RequestMapping("/api/rpm")
public class RpmRepositoryController {

    private final RepositoryChannelsService myRepositoryChannelsService;
    private final RpmDistributionRepository myRpmRepository;

    @Autowired
    public RpmRepositoryController(RepositoryChannelsService repositoryChannelsService,
                                   RpmDistributionRepository rpmRepository) {
        myRepositoryChannelsService = repositoryChannelsService;
        myRpmRepository = rpmRepository;
    }

    @GetMapping(value = "/{channel}/repodata/repomd.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> repomd(@PathVariable PluginChannel channel) {
        RpmDistributionRepository.RpmIndex index = myRpmRepository.getIndex(channel);
        if (index == null) return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        return ResponseEntity.ok(index.repomd());
    }

    @GetMapping("/{channel}/repodata/primary.xml.gz")
    public ResponseEntity<byte[]> primaryXmlGz(@PathVariable PluginChannel channel) {
        RpmDistributionRepository.RpmIndex index = myRpmRepository.getIndex(channel);
        if (index == null) return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        return ResponseEntity.ok().header("Content-Type", "application/gzip").body(index.primaryXmlGz());
    }

    /**
     * Filename format: {@code consulo-plugin-{pluginId}-{version}-1.noarch.rpm}
     */
    @GetMapping("/{channel}/{filename:.+}")
    public ResponseEntity<Void> download(@PathVariable PluginChannel channel, @PathVariable String filename) {
        if (!filename.endsWith("-1.noarch.rpm")) return ResponseEntity.notFound().build();
        String stem = filename.substring(0, filename.length() - "-1.noarch.rpm".length());
        if (!stem.startsWith("consulo-plugin-")) return ResponseEntity.notFound().build();
        int lastDash = stem.lastIndexOf('-');
        if (lastDash < 0) return ResponseEntity.notFound().build();

        String pluginId = stem.substring("consulo-plugin-".length(), lastDash);
        String version = stem.substring(lastDash + 1);

        PluginNode node = PackageRepositoryUtil.findPlugin(myRepositoryChannelsService, channel, pluginId, version);
        if (node == null) return ResponseEntity.notFound().build();

        String url = PackageRepositoryUtil.resolveDownloadUrl(channel, node);
        if (url == null) return ResponseEntity.notFound().build();
        return ResponseEntity.status(HttpStatus.FOUND).header("Location", url).build();
    }
}
