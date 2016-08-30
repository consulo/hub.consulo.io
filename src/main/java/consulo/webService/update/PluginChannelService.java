package consulo.webService.update;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.util.ThrowableConsumer;
import com.intellij.util.text.VersionComparatorUtil;
import consulo.webService.ChildService;
import consulo.webService.util.GsonUtil;

/**
 * @author VISTALL
 * @since 28-Aug-16
 */
public class PluginChannelService extends ChildService
{
	private static class PluginsState
	{
		private NavigableSet<PluginNode> myVersions = new TreeSet<>((o1, o2) -> VersionComparatorUtil.compare(o1.version, o2.version));

		private File myPluginDirectory;

		private String myPluginId;

		private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

		private PluginsState(File rootDir, String pluginId)
		{
			myPluginId = pluginId;
			myPluginDirectory = new File(rootDir, pluginId);

			FileUtilRt.createParentDirs(myPluginDirectory);
		}

		@NotNull
		public File getFileForPlugin(String version)
		{
			File zipFile = new File(myPluginDirectory, myPluginId + "_" + version + ".zip");
			FileUtilRt.createParentDirs(zipFile);
			if(zipFile.exists())
			{
				throw new IllegalArgumentException("Plugin " + myPluginId + "=" + version + " is already uploaded");
			}
			return zipFile;
		}
	}

	private File myPluginChannelDirectory;

	private final UpdateChannel myChannel;

	private Map<String, PluginsState> myPlugins = new ConcurrentHashMap<>();

	public PluginChannelService(UpdateChannel channel)
	{
		myChannel = channel;
	}

	public void push(PluginNode pluginNode, ThrowableConsumer<File, IOException> writeConsumer) throws IOException
	{
		PluginsState pluginsState = myPlugins.computeIfAbsent(pluginNode.id, id -> new PluginsState(myPluginChannelDirectory, pluginNode.id));

		ReentrantReadWriteLock.ReadLock writeLock = pluginsState.lock.readLock();
		try
		{
			writeLock.lock();

			File fileForPlugin = pluginsState.getFileForPlugin(pluginNode.version);

			writeConsumer.consume(fileForPlugin);


			pluginNode.length = fileForPlugin.length();
			pluginNode.targetFile = fileForPlugin;

			File metaFile = new File(fileForPlugin.getParentFile(), fileForPlugin.getName() + ".json");

			FileUtilRt.delete(metaFile);

			FileUtil.writeToFile(metaFile, GsonUtil.get().toJson(pluginNode));

			pluginsState.myVersions.add(pluginNode);
		}
		finally
		{
			writeLock.unlock();
		}
	}

	public UpdateChannel getChannel()
	{
		return myChannel;
	}

	@NotNull
	public File getPluginChannelDirectory()
	{
		return myPluginChannelDirectory;
	}

	@Override
	public void initImpl(File pluginChannelDir)
	{
		File channelDir = new File(pluginChannelDir, myChannel.name());

		FileUtil.createDirectory(channelDir);

		myPluginChannelDirectory = channelDir;
	}
}
