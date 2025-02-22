package consulo.hub.backend.repository.impl.store.neww;

import consulo.hub.backend.TempFileService;
import consulo.hub.backend.WorkDirectoryService;
import consulo.hub.backend.repository.impl.store.BaseRepositoryChannelStore;
import consulo.hub.backend.repository.impl.store.BaseRepositoryNodeState;
import consulo.hub.backend.util.GsonUtil;
import consulo.hub.shared.repository.PluginChannel;
import consulo.hub.shared.repository.PluginNode;
import consulo.hub.shared.repository.util.PlatformNodeDesc;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 18/05/2023
 */
public class NewInlineRepositoryStore {
    private record ConvertPlatformOldToNewId(Path jsonFilePath,
                                             PlatformNodeDesc platformNodeDesc) {
    }

    private static final Logger LOG = LoggerFactory.getLogger(NewInlineRepositoryStore.class);

    private final WorkDirectoryService myWorkDirectoryService;

    private final TempFileService myTempFileService;

    private Path myStorePath;

    private AtomicBoolean myLoading = new AtomicBoolean();

    public NewInlineRepositoryStore(WorkDirectoryService workDirectoryService, TempFileService tempFileService) {
        myWorkDirectoryService = workDirectoryService;
        myTempFileService = tempFileService;
    }

    public RepositoryNodeMeta updateMeta(String pluginId, String version, String ext, Consumer<RepositoryNodeMeta> consumer) throws IOException {
        Path pluginPath = workPath().resolve(pluginId);

        String fileName = pluginId + "_" + version + "." + ext;

        Path metaPath = pluginPath.resolve(fileName + ".json");

        RepositoryNodeMeta meta;
        if (Files.exists(metaPath)) {
            try (Reader reader = Files.newBufferedReader(metaPath)) {
                meta = GsonUtil.get().fromJson(reader, RepositoryNodeMeta.class);
            }
        }
        else {
            meta = new RepositoryNodeMeta();
        }

        consumer.accept(meta);

        Files.deleteIfExists(metaPath);

        String jsonText = GsonUtil.prettyGet().toJson(meta);

        Files.writeString(metaPath, jsonText, StandardCharsets.UTF_8);

        return meta;
    }

    @Nonnull
    public Path prepareArtifactPath(String pluginId, String version, String ext) throws IOException {
        Path pluginPath = workPath().resolve(pluginId);
        if (!Files.exists(pluginPath)) {
            Files.createDirectory(pluginPath);
        }

        String fileName = pluginId + "_" + version + "." + ext;

        Path artifactPath = pluginPath.resolve(fileName);

        Path metaPath = pluginPath.resolve(fileName + ".json");

        if (Files.exists(artifactPath)) {
            if (Files.exists(metaPath)) {
                throw new RedeployException("Plugin " + pluginId + "=" + version + " is already uploaded");
            }
            else {
                LOG.warn("Zombie archive was deleted: " + artifactPath.toAbsolutePath());
                Files.delete(artifactPath);
            }
        }

        return artifactPath;
    }

    @Nonnull
    public Path workPath() {
        return Objects.requireNonNull(myStorePath, "not initialized");
    }

    public boolean init() throws Exception {
        Path workPath = myWorkDirectoryService.getWorkingDirectory().resolve("inlineStore");

        boolean isNewStore = !Files.exists(workPath);

        if (isNewStore) {
            Files.createDirectories(workPath);
        }

        myStorePath = workPath;

        return isNewStore;
    }

    public void load(NewRepositoryChannelsService repositoryChannelsService) {
        List<ConvertPlatformOldToNewId> toConvert = new ArrayList<>();

        long time = System.currentTimeMillis();

        try {
            myLoading.set(true);

            List<Path> paths = Files.walk(workPath(), 1).filter(it -> !it.equals(workPath())).toList();

            paths.parallelStream().forEach(path -> {
                try {
                    PlatformNodeDesc nodeByOldId = PlatformNodeDesc.findByOldId(path.getFileName().toString());
                    if (nodeByOldId != null) {
                        boolean[] found = new boolean[1];

                        // that old platform we need migrate to new id
                        Files.walkFileTree(path, new SimpleFileVisitor<>() {
                            @Override
                            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                                if (file.getFileName().toString().endsWith(".json")) {
                                    toConvert.add(new ConvertPlatformOldToNewId(file, nodeByOldId));
                                    found[0] = true;
                                }
                                return FileVisitResult.CONTINUE;
                            }
                        });

                        if (!found[0]) {
                            try {
                                FileSystemUtils.deleteRecursively(path);
                                LOG.info("Remove old path which not used anymore {}", path);
                            }
                            catch (IOException ignored) {
                            }
                        }
                    }
                    else {
                        Files.walkFileTree(path, new SimpleFileVisitor<>() {
                            @Override
                            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                                if (file.getFileName().toString().endsWith(".json")) {
                                    processJsonFile(file, repositoryChannelsService);
                                }
                                return FileVisitResult.CONTINUE;
                            }
                        });
                    }
                }
                catch (IOException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            });

            toConvert.parallelStream().forEach(oldToNewId -> {
                try {
                    convertToNewPlatformId(oldToNewId.jsonFilePath(), oldToNewId.platformNodeDesc(), repositoryChannelsService);
                }
                catch (IOException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            });

            if (!toConvert.isEmpty()) {
                LOG.info("Finished converting {} items.", toConvert.size());
            }
        }
        catch (Throwable e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        finally {
            myLoading.set(false);
        }

        long diff = (System.currentTimeMillis() - time) / 1000L;

        LOG.info("Finished loading repository state in {} seconds", diff);
    }

    @Nullable
    private RepositoryNodeMeta readJson(Path jsonFilePath) {
        String path = jsonFilePath.toAbsolutePath().toString();

        RepositoryNodeMeta meta;
        try (Reader fileReader = Files.newBufferedReader(jsonFilePath, StandardCharsets.UTF_8)) {
            meta = GsonUtil.get().fromJson(fileReader, RepositoryNodeMeta.class);
        }
        catch (IOException e) {
            LOG.error(e.getMessage(), e);
            return null;
        }

        String fileName = jsonFilePath.getFileName().toString();

        Path parentDir = jsonFilePath.getParent();

        String artifactFileStr = fileName.substring(0, fileName.length() - 5);

        Path targetArtifact = parentDir.resolve(artifactFileStr);

        if (!Files.exists(targetArtifact)) {
            try {
                Files.delete(jsonFilePath);

                LOG.warn("Zombie json file: " + path);
            }
            catch (IOException e) {
                LOG.error(path, e);
            }
            return null;
        }

        meta.node.targetPath = targetArtifact;
        return meta;
    }

    private void convertToNewPlatformId(Path jsonFilePath,
                                        PlatformNodeDesc platformNodeDesc,
                                        NewRepositoryChannelsService repositoryChannelsService) throws IOException {
        RepositoryNodeMeta meta = readJson(jsonFilePath);
        if (meta == null) {
            return;
        }

        LOG.info("Converting {} platform to {} new id, version {}", meta.node.id, platformNodeDesc.id(), meta.node.platformVersion);

        Path metaFile = meta.node.targetPath;
        if (metaFile == null && meta.node.targetFile != null) {
            metaFile = meta.node.targetFile.toPath();
        }

        if (metaFile == null) {
            throw new IllegalArgumentException("There no archive for platform: " + meta.node.id);
        }

        String nodeId = platformNodeDesc.id();
        String ext = platformNodeDesc.ext();
        try {
            Path path = prepareArtifactPath(nodeId, meta.node.version, ext);

            Files.copy(metaFile, path, StandardCopyOption.COPY_ATTRIBUTES);

            RepositoryNodeMeta resultMeta = updateMeta(nodeId, meta.node.version, ext, newMeta -> {
                PluginNode cloned = meta.node.clone();

                BaseRepositoryNodeState.prepareNode(cloned, path);

                cloned.id = nodeId;
                cloned.name = platformNodeDesc.name();

                newMeta.node = cloned;

                newMeta.channels.addAll(meta.channels);
            });

            for (PluginChannel channel : resultMeta.channels) {
                BaseRepositoryChannelStore store = (BaseRepositoryChannelStore) repositoryChannelsService.getRepositoryByChannel(channel);

                PluginNode newPluginNode = resultMeta.node.clone();
                newPluginNode.targetPath = resultMeta.node.targetPath;

                store._add(newPluginNode);
            }
        }
        catch (RedeployException ignored) {
            // since it's already deployed - just remove files
        }

        Files.deleteIfExists(metaFile);

        Files.deleteIfExists(jsonFilePath);

        LOG.info("Finished converting {} platform to {} new id, version {}", meta.node.id, platformNodeDesc.id(), meta.node.platformVersion);
    }

    private void processJsonFile(Path jsonFilePath, NewRepositoryChannelsService repositoryChannelsService) {
        RepositoryNodeMeta meta = readJson(jsonFilePath);
        if (meta == null) {
            return;
        }

        for (PluginChannel channel : meta.channels) {
            BaseRepositoryChannelStore store = (BaseRepositoryChannelStore) repositoryChannelsService.getRepositoryByChannel(channel);

            PluginNode newPluginNode = meta.node.clone();
            newPluginNode.targetPath = meta.node.targetPath;

            store._add(newPluginNode);
        }
    }

    public boolean isLoading() {
        return myLoading.get();
    }
}
