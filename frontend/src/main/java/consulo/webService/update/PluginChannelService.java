package consulo.webService.update;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.util.CommonProcessors;
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
		private final NavigableMap<String, NavigableSet<PluginNode>> myPluginsByPlatformVersion = new TreeMap<>();

		private final File myPluginDirectory;

		private final String myPluginId;

		private final ReentrantReadWriteLock myLock = new ReentrantReadWriteLock();

		private PluginsState(File rootDir, String pluginId)
		{
			myPluginId = pluginId;
			myPluginDirectory = new File(rootDir, pluginId);

			FileUtilRt.createParentDirs(myPluginDirectory);
		}

		// required write lock
		public void add(PluginNode pluginNode)
		{
			NavigableSet<PluginNode> nodes = myPluginsByPlatformVersion.computeIfAbsent(pluginNode.platformVersion, s -> newTreeSet());

			nodes.add(pluginNode);
		}

		private static NavigableSet<PluginNode> newTreeSet()
		{
			return new TreeSet<>((o1, o2) -> VersionComparatorUtil.compare(o1.version, o2.version));
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

	private static final Logger LOGGER = Logger.getInstance(PluginChannelService.class);
	public static final String SNAPSHOT = "SNAPSHOT";

	private File myPluginChannelDirectory;

	private final UpdateChannel myChannel;

	private Map<String, PluginsState> myPlugins = new ConcurrentSkipListMap<>();

	public PluginChannelService(UpdateChannel channel)
	{
		myChannel = channel;
	}

	@Nullable
	public PluginNode select(@NotNull String platformVersion, @Nullable String pluginId)
	{
		PluginsState state = myPlugins.get(pluginId);
		if(state == null)
		{
			return null;
		}

		state.myLock.readLock().lock();
		try
		{
			NavigableSet<PluginNode> pluginNodes = getPluginSetByVersion(platformVersion, state);
			if(pluginNodes == null || pluginNodes.isEmpty())
			{
				return null;
			}

			return pluginNodes.last();
		}
		finally
		{
			state.myLock.readLock().unlock();
		}
	}

	@NotNull
	public PluginNode[] select(@NotNull String platformVersion)
	{
		List<PluginNode> list = new ArrayList<>();
		for(PluginsState state : myPlugins.values())
		{
			state.myLock.readLock().lock();
			try
			{
				NavigableSet<PluginNode> pluginNodes = getPluginSetByVersion(platformVersion, state);
				if(pluginNodes == null || pluginNodes.isEmpty())
				{
					continue;
				}

				PluginNode last = pluginNodes.last();
				list.add(last.clone());
			}
			finally
			{
				state.myLock.readLock().unlock();
			}
		}
		return list.isEmpty() ? PluginNode.EMPTY_ARRAY : list.toArray(new PluginNode[list.size()]);
	}

	// guarded by lock
	@Nullable
	private NavigableSet<PluginNode> getPluginSetByVersion(@NotNull String platformVersion, PluginsState state)
	{
		NavigableMap<String, NavigableSet<PluginNode>> map = state.myPluginsByPlatformVersion;
		if(SNAPSHOT.equals(platformVersion))
		{
			Map.Entry<String, NavigableSet<PluginNode>> entry = map.lastEntry();
			return entry == null ? null : entry.getValue();
		}
		return map.get(platformVersion);
	}


	public void push(PluginNode pluginNode, ThrowableConsumer<File, IOException> writeConsumer) throws IOException
	{
		PluginsState pluginsState = myPlugins.computeIfAbsent(pluginNode.id, id -> new PluginsState(myPluginChannelDirectory, pluginNode.id));

		ReentrantReadWriteLock.ReadLock writeLock = pluginsState.myLock.readLock();
		writeLock.lock();
		try
		{

			File fileForPlugin = pluginsState.getFileForPlugin(pluginNode.version);

			writeConsumer.consume(fileForPlugin);

			pluginNode.length = fileForPlugin.length();
			pluginNode.targetFile = fileForPlugin;
			pluginNode.clean();

			File metaFile = new File(fileForPlugin.getParentFile(), fileForPlugin.getName() + ".json");

			FileUtilRt.delete(metaFile);

			FileUtil.writeToFile(metaFile, GsonUtil.get().toJson(pluginNode));

			pluginsState.add(pluginNode);
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

		CommonProcessors.CollectProcessor<File> processor = new CommonProcessors.CollectProcessor<>();
		FileUtil.visitFiles(myPluginChannelDirectory, processor);

		processor.getResults().parallelStream().forEach(file -> {
			if(file.getName().endsWith("zip.json"))
			{
				try
				{
					processJsonFile(file);
				}
				catch(Exception e)
				{
					LOGGER.error(e.getMessage(), e);
				}
			}
		});
	}

	private void processJsonFile(File jsonFile) throws Exception
	{
		String path = jsonFile.getPath();
		LOGGER.info("Analyze: " + path);

		PluginNode pluginNode = GsonUtil.get().fromJson(new FileReader(jsonFile), PluginNode.class);
		pluginNode.clean();

		String name = jsonFile.getName();
		File zipFile = new File(jsonFile.getParentFile(), name.substring(0, name.length() - 5));
		if(!zipFile.exists())
		{
			LOGGER.warn("Zombie json file: " + path);
			return;
		}

		PluginsState pluginsState = myPlugins.computeIfAbsent(pluginNode.id, id -> new PluginsState(myPluginChannelDirectory, pluginNode.id));

		ReentrantReadWriteLock.ReadLock writeLock = pluginsState.myLock.readLock();
		try
		{
			writeLock.lock();

			pluginNode.length = zipFile.length();
			pluginNode.targetFile = zipFile;

			pluginsState.add(pluginNode);
		}
		finally
		{
			writeLock.unlock();
		}
	}
}
