package consulo.hub.backend.repository.impl.store.neww;

import consulo.hub.backend.TempFileService;
import consulo.hub.backend.WorkDirectoryService;
import consulo.hub.backend.repository.archive.ArchiveData;
import consulo.hub.backend.repository.impl.store.BaseRepositoryChannelStore;
import consulo.hub.backend.repository.impl.store.BaseRepositoryNodeState;
import consulo.hub.backend.repository.impl.store.old.OldPluginChannelService;
import consulo.hub.backend.util.GsonUtil;
import consulo.hub.shared.repository.PluginChannel;
import consulo.hub.shared.repository.PluginNode;
import consulo.hub.shared.repository.util.PlatformNodeDesc;
import jakarta.annotation.Nonnull;
import org.apache.commons.compress.archivers.ArchiveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 18/05/2023
 */
public class NewInlineRepositoryStore
{
	private static final Logger LOG = LoggerFactory.getLogger(NewInlineRepositoryStore.class);

	private final WorkDirectoryService myWorkDirectoryService;

	private final TempFileService myTempFileService;

	private Path myStorePath;

	private AtomicBoolean myLoading = new AtomicBoolean();

	public NewInlineRepositoryStore(WorkDirectoryService workDirectoryService, TempFileService tempFileService)
	{
		myWorkDirectoryService = workDirectoryService;
		myTempFileService = tempFileService;
	}

	public void updateMeta(String pluginId, String version, String ext, Consumer<RepositoryNodeMeta> consumer) throws IOException
	{
		Path pluginPath = workPath().resolve(pluginId);

		String fileName = pluginId + "_" + version + "." + ext;

		Path metaPath = pluginPath.resolve(fileName + ".json");

		RepositoryNodeMeta meta;
		if(Files.exists(metaPath))
		{
			try (Reader reader = Files.newBufferedReader(metaPath))
			{
				meta = GsonUtil.get().fromJson(reader, RepositoryNodeMeta.class);
			}
		}
		else
		{
			meta = new RepositoryNodeMeta();
		}

		consumer.accept(meta);

		Files.deleteIfExists(metaPath);

		String jsonText = GsonUtil.prettyGet().toJson(meta);

		Files.writeString(metaPath, jsonText, StandardCharsets.UTF_8);
	}

	@Nonnull
	public Path prepareArtifactPath(String pluginId, String version, String ext) throws IOException
	{
		Path pluginPath = workPath().resolve(pluginId);
		if(!Files.exists(pluginPath))
		{
			Files.createDirectory(pluginPath);
		}

		String fileName = pluginId + "_" + version + "." + ext;

		Path artifactPath = pluginPath.resolve(fileName);

		Path metaPath = pluginPath.resolve(fileName + ".json");

		if(Files.exists(artifactPath))
		{
			if(Files.exists(metaPath))
			{
				throw new IllegalArgumentException("Plugin " + pluginId + "=" + version + " is already uploaded");
			}
			else
			{
				LOG.warn("Zombie archive was deleted: " + artifactPath.toAbsolutePath());
				Files.delete(artifactPath);
			}
		}

		return artifactPath;
	}

	@Nonnull
	public Path workPath()
	{
		return Objects.requireNonNull(myStorePath, "not initialized");
	}

	public boolean init() throws Exception
	{
		Path workPath = myWorkDirectoryService.getWorkingDirectory().resolve("inlineStore");

		boolean isNewStore = !Files.exists(workPath);

		if(isNewStore)
		{
			Files.createDirectories(workPath);
		}

		myStorePath = workPath;

		return isNewStore;
	}

	public void runImport(NewRepositoryChannelsService repositoryChannelsService)
	{
		try
		{
			myLoading.set(true);

			Path pluginPath = myWorkDirectoryService.getWorkingDirectory().resolve("plugin");
			if(!Files.exists(pluginPath))
			{
				// there no plugin dir - skip
				return;
			}

			ImportPluginJoiner joiner = new ImportPluginJoiner();

			for(PluginChannel channel : PluginChannel.values())
			{
				Path channelDir = pluginPath.resolve(channel.name());
				if(!Files.exists(channelDir))
				{
					continue;
				}

				OldPluginChannelService oldService = new OldPluginChannelService(channel);
				oldService.initImpl(pluginPath.toFile());

				oldService.iteratePluginNodes(pluginNode ->
				{
					joiner.join(pluginNode, channel);
				});
			}

			joiner.getNodes().entrySet().stream().parallel().forEach(entry ->
			{
				try
				{
					RepositoryNodeMeta meta = entry.getValue();

					LOG.info("Importing " + entry.getKey());

					final String originalPluginId = meta.node.id;
					PlatformNodeDesc platformNodeDesc = PlatformNodeDesc.findByOldId(originalPluginId);
					if(platformNodeDesc != null)
					{
						Path tempExtractPath = myTempFileService.createTempDirPath("deploy_platform_extract");

						try
						{
							ArchiveData archive = new ArchiveData();

							archive.extract(meta.node.targetFile, tempExtractPath.toFile());

							// remove old plugin channel markets
							for(PluginChannel pluginChannel : PluginChannel.values())
							{
								archive.removeEntry(makePluginChannelFileName(originalPluginId, pluginChannel));
							}

							String nodeId = platformNodeDesc.id();
							String ext = platformNodeDesc.ext();
							Path path = prepareArtifactPath(nodeId, meta.node.version, ext);

							archive.create(path, ext.equals("tar.gz") ? "tar" : ext);

							updateMeta(nodeId, meta.node.version, ext, newMeta ->
							{
								PluginNode cloned = meta.node.clone();

								BaseRepositoryNodeState.prepareNode(cloned, path);

								cloned.id = nodeId;
								cloned.name = platformNodeDesc.name();

								newMeta.node = cloned;

								newMeta.channels.addAll(meta.channels);
							});
						}
						finally
						{
							myTempFileService.asyncDelete(tempExtractPath);
						}
					}
					else
					{
						String ext = repositoryChannelsService.getDeployPluginExtension();

						Path path = prepareArtifactPath(originalPluginId, meta.node.version, ext);

						Files.copy(meta.node.targetFile.toPath(), path);

						updateMeta(originalPluginId, meta.node.version, ext, newMeta ->
						{
							newMeta.node = meta.node;
							newMeta.channels.addAll(meta.channels);
						});
					}
				}
				catch(IOException | ArchiveException e)
				{
					LOG.error(entry.getKey().toString(), e);
				}
			});
		}
		catch(Throwable e)
		{
			LOG.error(e.getLocalizedMessage(), e);
		}
		finally
		{
			myLoading.set(false);
		}
	}

	public static String makePluginChannelFileName(String pluginId, PluginChannel pluginChannel)
	{
		boolean mac = pluginId.startsWith("consulo-mac");
		if(mac)
		{
			return "Consulo.app/Contents/." + pluginChannel.name();
		}
		else
		{
			return "Consulo/." + pluginChannel.name();
		}
	}

	public void load(NewRepositoryChannelsService repositoryChannelsService)
	{
		try
		{
			myLoading.set(true);

			List<Path> paths = Files.walk(workPath(), 1).filter(it -> !it.equals(workPath())).toList();

			paths.parallelStream().forEach(path ->
			{
				try
				{
					Files.walkFileTree(path, new SimpleFileVisitor<>()
					{
						@Override
						public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
						{
							if(file.getFileName().toString().endsWith(".json"))
							{
								processJsonFile(file, repositoryChannelsService);
							}
							return FileVisitResult.CONTINUE;
						}
					});
				}
				catch(IOException e)
				{
					LOG.error(e.getLocalizedMessage(), e);
				}
			});
		}
		catch(Throwable e)
		{
			LOG.error(e.getLocalizedMessage(), e);
		}
		finally
		{
			myLoading.set(false);
		}
	}

	private void processJsonFile(Path jsonFilePath, NewRepositoryChannelsService repositoryChannelsService)
	{
		String path = jsonFilePath.toAbsolutePath().toString();

		RepositoryNodeMeta meta;
		try (Reader fileReader = Files.newBufferedReader(jsonFilePath, StandardCharsets.UTF_8))
		{
			meta = GsonUtil.get().fromJson(fileReader, RepositoryNodeMeta.class);
		}
		catch(IOException e)
		{
			LOG.error(e.getMessage(), e);
			return;
		}

		String fileName = jsonFilePath.getFileName().toString();

		Path parentDir = jsonFilePath.getParent();

		String artifactFileStr = fileName.substring(0, fileName.length() - 5);

		Path targetArtifact = parentDir.resolve(artifactFileStr);

		if(!Files.exists(targetArtifact))
		{
			try
			{
				Files.delete(jsonFilePath);

				LOG.warn("Zombie json file: " + path);
			}
			catch(IOException e)
			{
				LOG.error(path, e);
			}
			return;
		}

		meta.node.targetPath = targetArtifact;

		for(PluginChannel channel : meta.channels)
		{
			BaseRepositoryChannelStore store = (BaseRepositoryChannelStore) repositoryChannelsService.getRepositoryByChannel(channel);

			PluginNode newPluginNode = meta.node.clone();
			newPluginNode.targetPath = targetArtifact;

			store._add(newPluginNode);
		}
	}

	public boolean isLoading()
	{
		return myLoading.get();
	}
}
