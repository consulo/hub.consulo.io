package consulo.hub.backend.repository.external;

import consulo.hub.backend.WorkDirectoryService;
import consulo.hub.shared.repository.PluginNode;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Disk cache for pre-built native Linux packages (.deb, .pkg.tar.gz).
 * Building and scheduling is handled by {@link PackageRebuilderService}.
 *
 * @author VISTALL
 */
@Service
public class PluginPackageStore {
    private static final Logger LOG = LoggerFactory.getLogger(PluginPackageStore.class);

    private final WorkDirectoryService myWorkDirectoryService;

    @Autowired
    public PluginPackageStore(@Nonnull WorkDirectoryService workDirectoryService) {
        myWorkDirectoryService = workDirectoryService;
    }

    // ---- Plugin read ----

    @Nullable
    public byte[] getDeb(@Nonnull PluginNode node) {
        return readCache(pluginPath(node, "deb"));
    }

    @Nullable
    public byte[] getPkg(@Nonnull PluginNode node) {
        return readCache(pluginPath(node, "pkg.tar.gz"));
    }

    public boolean hasPlugin(@Nonnull PluginNode node) {
        return Files.exists(pluginPath(node, "deb")) && Files.exists(pluginPath(node, "pkg.tar.gz"));
    }

    // ---- Platform read ----

    @Nullable
    public byte[] getPlatformDeb(@Nonnull PluginNode node) {
        return readCache(platformPath(node, "deb"));
    }

    @Nullable
    public byte[] getPlatformPkg(@Nonnull PluginNode node) {
        return readCache(platformPath(node, "pkg.tar.gz"));
    }

    public boolean hasPlatform(@Nonnull PluginNode node) {
        return Files.exists(platformPath(node, "deb")) && Files.exists(platformPath(node, "pkg.tar.gz"));
    }

    // ---- Path accessors for PackageRebuilderService ----

    @Nonnull
    public Path pluginPath(@Nonnull PluginNode node, @Nonnull String ext) {
        return myWorkDirectoryService.getWorkingDirectory()
            .resolve("inlineStore")
            .resolve(node.id)
            .resolve(node.id + "_" + node.version + "." + ext);
    }

    @Nonnull
    public Path platformPath(@Nonnull PluginNode node, @Nonnull String ext) {
        return myWorkDirectoryService.getWorkingDirectory()
            .resolve("platformStore")
            .resolve(node.id)
            .resolve(node.id + "_" + node.version + "." + ext);
    }

    // ---- Internal ----

    @Nullable
    private byte[] readCache(@Nonnull Path path) {
        if (!Files.exists(path)) return null;
        try {
            return Files.readAllBytes(path);
        }
        catch (IOException e) {
            LOG.warn("Failed to read package {}: {}", path.getFileName(), e.getMessage());
            return null;
        }
    }
}
