package consulo.hub.backend.repository.impl.store.old;

import consulo.hub.backend.repository.RepositoryNodeState;
import consulo.hub.backend.repository.impl.store.BaseRepositoryNodeState;
import consulo.hub.backend.util.AccessToken;
import consulo.hub.backend.util.GsonUtil;
import consulo.hub.shared.repository.PluginNode;
import consulo.util.io.FileUtil;
import consulo.util.lang.Pair;
import consulo.util.lang.function.ThrowableConsumer;
import consulo.util.lang.function.ThrowableFunction;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableSet;

/**
 * @author VISTALL
 * @since 09-May-17
 */
@Deprecated
public class OldPluginsState extends BaseRepositoryNodeState implements RepositoryNodeState
{
	private static final Logger logger = LoggerFactory.getLogger(OldPluginsState.class);

	protected final File myPluginDirectory;

	protected OldPluginsState(File rootDir, String pluginId)
	{
		super(pluginId);
		myPluginDirectory = new File(rootDir, pluginId);
	}

	/**
	 * @return lock free plugins state
	 */
	@Nonnull
	public OldPluginsState copy()
	{
		OldPluginsState copy = new OldPluginsState(myPluginDirectory, myPluginId);

		try (AccessToken ignored = readLock())
		{
			for(Map.Entry<String, NavigableSet<PluginNode>> entry : myPluginsByPlatformVersion.entrySet())
			{
				for(PluginNode node : entry.getValue())
				{
					copy._add(node);
				}
			}
		}
		return copy;
	}

	@Nonnull
	public File getFileForPlugin(String version, String ext)
	{
		String fileName = myPluginId + "_" + version + "." + ext;
		File artifactFile = new File(myPluginDirectory, fileName);
		FileUtil.createParentDirs(artifactFile);
		if(artifactFile.exists())
		{
			File jsonFile = new File(myPluginDirectory, fileName + ".json");
			if(jsonFile.exists())
			{
				throw new IllegalArgumentException("Plugin " + myPluginId + "=" + version + " is already uploaded");
			}
			else
			{
				logger.warn("Zombie archive was deleted: " + artifactFile.getPath());
				artifactFile.delete();
			}
		}
		return artifactFile;
	}

	@Override
	protected void removeRepositoryArtifact(PluginNode target)
	{
		File targetFile = target.targetFile;
		// in tests target file is null
		if(targetFile != null)
		{
			targetFile.delete();

			File jsonFile = new File(targetFile.getParentFile(), targetFile.getName() + ".json");

			jsonFile.delete();
		}
	}

	@Override
	public void push(PluginNode pluginNode, String ext, ThrowableConsumer<Path, Exception> writeConsumer) throws Exception
	{
		try (AccessToken ignored = writeLock())
		{
			File fileForPlugin = getFileForPlugin(pluginNode.version, ext);

			Path nioPath = fileForPlugin.toPath();

			writeConsumer.consume(nioPath);

			prepareNode(pluginNode, nioPath);

			pluginNode.length = fileForPlugin.length();
			pluginNode.targetFile = fileForPlugin;
			pluginNode.cleanUp();

			File metaFile = new File(fileForPlugin.getParentFile(), fileForPlugin.getName() + ".json");

			FileSystemUtils.deleteRecursively(metaFile);

			FileUtil.writeToFile(metaFile, GsonUtil.get().toJson(pluginNode));

			_add(pluginNode);
		}
	}

	private String calculateChecksum(File input, ThrowableFunction<InputStream, String, Exception> digFunc)
	{
		try(FileInputStream in = new FileInputStream(input))
		{
			return digFunc.apply(in).toUpperCase(Locale.ROOT);
		}
		catch(Exception e)
		{
			logger.error("Can't calculate checksum " + input.getPath(), e);
			return "__error__";
		}
	}

	public void processEntry(Map.Entry<String, List<Pair<PluginNode, File>>> entry)
	{
		List<Pair<PluginNode, File>> value = entry.getValue();

		try (AccessToken ignored = writeLock())
		{
			for(Pair<PluginNode, File> pair : value)
			{
				PluginNode pluginNode = pair.getFirst();
				File targetArchive = pair.getSecond();

				pluginNode.length = targetArchive.length();
				pluginNode.targetFile = targetArchive;

				_add(pluginNode);
			}
		}
	}
}
