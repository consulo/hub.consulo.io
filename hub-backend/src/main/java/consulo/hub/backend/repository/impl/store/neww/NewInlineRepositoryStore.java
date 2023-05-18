package consulo.hub.backend.repository.impl.store.neww;

import consulo.hub.backend.WorkDirectoryService;
import consulo.hub.backend.util.GsonUtil;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 18/05/2023
 */
public class NewInlineRepositoryStore
{
	private static final Logger LOG = LoggerFactory.getLogger(NewInlineRepositoryStore.class);

	private final WorkDirectoryService myWorkDirectoryService;

	private Path myStorePath;

	public NewInlineRepositoryStore(WorkDirectoryService workDirectoryService)
	{
		myWorkDirectoryService = workDirectoryService;
	}

	public void updateMeta(String pluginId, String version, String ext, Consumer<RepositoryNodeMeta> consumer) throws IOException
	{
		Path pluginPath = workPath().resolve(pluginId);

		String fileName = pluginId + "_" + version + "." + ext;

		Path metaPath = pluginPath.resolve(fileName + ".json");

		RepositoryNodeMeta meta = new RepositoryNodeMeta();
		consumer.accept(meta);

		Files.deleteIfExists(metaPath);

		String jsonText = GsonUtil.prettyGet().toJson(meta);

		Files.writeString(metaPath, jsonText);
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
			Files.createDirectory(workPath);

			// TODO migration
		}

		myStorePath = workPath;

		return isNewStore;
	}
}
