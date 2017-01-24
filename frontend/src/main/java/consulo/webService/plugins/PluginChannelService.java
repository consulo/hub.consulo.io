package consulo.webService.plugins;

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
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileSystemUtils;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.util.ArrayUtil;
import com.intellij.util.CommonProcessors;
import com.intellij.util.ThrowableConsumer;
import com.intellij.util.text.VersionComparatorUtil;
import consulo.webService.util.GsonUtil;

/**
 * @author VISTALL
 * @since 28-Aug-16
 */
public class PluginChannelService
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
			NavigableSet<PluginNode> nodes = myPluginsByPlatformVersion.computeIfAbsent(pluginNode.platformVersion, PluginsState::newTreeSet);

			nodes.add(pluginNode);
		}

		@NotNull
		public List<PluginNode> getAll()
		{
			ReentrantReadWriteLock.ReadLock readLock = myLock.readLock();
			readLock.lock();
			try
			{
				List<PluginNode> list = new ArrayList<>();
				for(NavigableSet<PluginNode> pluginNodes : myPluginsByPlatformVersion.values())
				{
					list.addAll(pluginNodes);
				}
				return list;
			}
			finally
			{
				readLock.unlock();
			}
		}

		private static NavigableSet<PluginNode> newTreeSet(@NotNull String unused)
		{
			return new TreeSet<>((o1, o2) -> VersionComparatorUtil.compare(o1.version, o2.version));
		}

		@NotNull
		public File getFileForPlugin(String version, String ext)
		{
			String fileName = myPluginId + "_" + version + "." + ext;
			File artifactFile = new File(myPluginDirectory, fileName);
			FileUtilRt.createParentDirs(artifactFile);
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
	}

	public static final String ourStandardWinId = "consulo-win-no-jre";
	public static final String ourStandardLinuxId = "consulo-linux-no-jre";
	public static final String ourStandardMacId = "consulo-mac-no-jre";

	private static final String[] ourPlatformPluginIds = {
			ourStandardWinId,
			"consulo-win",
			"consulo-win64",
			ourStandardLinuxId,
			"consulo-linux",
			"consulo-linux64",
			ourStandardMacId,
			"consulo-mac64",
			// special case for windows
			ourStandardWinId + "-zip",
			"consulo-win" + "-zip",
			"consulo-win64" + "-zip",
	};

	public static boolean isPlatformNode(String pluginId)
	{
		return ArrayUtil.contains(pluginId, ourPlatformPluginIds);
	}

	private static final Logger logger = LoggerFactory.getLogger(PluginChannelService.class);
	public static final String SNAPSHOT = "SNAPSHOT";

	private File myPluginChannelDirectory;

	private final PluginChannel myChannel;

	private final Map<String, PluginsState> myPlugins = new ConcurrentSkipListMap<>();

	public PluginChannelService(PluginChannel channel)
	{
		myChannel = channel;
	}

	public boolean isInRepository(String pluginId, String version, String platformVersion)
	{
		PluginsState state = myPlugins.get(pluginId);
		if(state == null)
		{
			return false;
		}

		state.myLock.readLock().lock();
		try
		{
			NavigableSet<PluginNode> nodes = state.myPluginsByPlatformVersion.get(platformVersion);

			if(nodes == null)
			{
				return false;
			}

			for(PluginNode node : nodes)
			{
				if(Comparing.equal(version, node.version))
				{
					return true;
				}
			}

			return false;
		}
		finally
		{
			state.myLock.readLock().unlock();
		}
	}

	public void remove(String pluginId, String version, String platformVersion)
	{
		PluginsState state = myPlugins.get(pluginId);
		if(state == null)
		{
			return;
		}

		ReentrantReadWriteLock.WriteLock writeLock = state.myLock.writeLock();
		writeLock.lock();
		try
		{
			NavigableSet<PluginNode> nodes = state.myPluginsByPlatformVersion.get(platformVersion);

			if(nodes == null)
			{
				return;
			}

			PluginNode target = null;
			for(PluginNode node : nodes)
			{
				if(Comparing.equal(version, node.version))
				{
					target = node;
					break;
				}
			}

			if(target != null)
			{
				nodes.remove(target);

				File targetFile = target.targetFile;

				targetFile.delete();

				File jsonFile = new File(targetFile.getParentFile(), targetFile.getName() + ".json");
				jsonFile.delete();
			}
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Nullable
	public PluginNode select(@NotNull String platformVersion, @NotNull String pluginId, @Nullable String version, boolean platformBuildSelect)
	{
		PluginsState state = myPlugins.get(pluginId);
		if(state == null)
		{
			return null;
		}

		state.myLock.readLock().lock();
		try
		{
			NavigableSet<PluginNode> pluginNodes = getPluginSetByVersion(platformVersion, state, platformBuildSelect);
			if(pluginNodes == null || pluginNodes.isEmpty())
			{
				return null;
			}

			if(version != null)
			{
				for(PluginNode pluginNode : pluginNodes)
				{
					if(Comparing.equal(pluginNode.version, version))
					{
						return pluginNode;
					}
				}

				return null;
			}
			else
			{
				return pluginNodes.last();
			}
		}
		finally
		{
			state.myLock.readLock().unlock();
		}
	}

	@NotNull
	public PluginNode[] select(@NotNull PluginStatisticsService statisticsService, @NotNull String platformVersion, boolean platformBuildSelect)
	{
		List<PluginNode> list = new ArrayList<>();
		for(PluginsState state : myPlugins.values())
		{
			state.myLock.readLock().lock();
			try
			{
				NavigableSet<PluginNode> pluginNodes = getPluginSetByVersion(platformVersion, state, platformBuildSelect);
				if(pluginNodes == null || pluginNodes.isEmpty())
				{
					continue;
				}

				PluginNode last = pluginNodes.last();

				PluginNode lastCloned = last.clone();
				lastCloned.downloads = statisticsService.getDownloadStat(last.id).size();
				list.add(lastCloned);
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
	private NavigableSet<PluginNode> getPluginSetByVersion(@NotNull String platformVersion, @NotNull PluginsState state, boolean platformBuildSelect)
	{
		NavigableMap<String, NavigableSet<PluginNode>> map = state.myPluginsByPlatformVersion;
		if(SNAPSHOT.equals(platformVersion) || !platformBuildSelect && isPlatformNode(state.myPluginId))
		{
			Map.Entry<String, NavigableSet<PluginNode>> entry = map.lastEntry();
			return entry == null ? null : entry.getValue();
		}
		return map.get(platformVersion);
	}

	public void iteratePluginNodes(@NotNull Consumer<PluginNode> consumer)
	{
		for(PluginsState pluginsState : myPlugins.values())
		{
			List<PluginNode> nodes = pluginsState.getAll();
			nodes.forEach(consumer);
		}
	}

	public void push(PluginNode pluginNode, String ext, ThrowableConsumer<File, Exception> writeConsumer) throws Exception
	{
		PluginsState pluginsState = myPlugins.computeIfAbsent(pluginNode.id, id -> new PluginsState(myPluginChannelDirectory, pluginNode.id));

		ReentrantReadWriteLock.WriteLock writeLock = pluginsState.myLock.writeLock();
		writeLock.lock();
		try
		{

			File fileForPlugin = pluginsState.getFileForPlugin(pluginNode.version, ext);

			writeConsumer.consume(fileForPlugin);

			pluginNode.date = System.currentTimeMillis();
			pluginNode.length = fileForPlugin.length();
			pluginNode.targetFile = fileForPlugin;
			pluginNode.clean();

			File metaFile = new File(fileForPlugin.getParentFile(), fileForPlugin.getName() + ".json");

			FileSystemUtils.deleteRecursively(metaFile);

			FileUtil.writeToFile(metaFile, GsonUtil.get().toJson(pluginNode));

			pluginsState.add(pluginNode);
		}
		finally
		{
			writeLock.unlock();
		}
	}

	public void initImpl(File pluginChannelDir)
	{
		File channelDir = new File(pluginChannelDir, myChannel.name());

		FileUtil.createDirectory(channelDir);

		myPluginChannelDirectory = channelDir;

		CommonProcessors.CollectProcessor<File> processor = new CommonProcessors.CollectProcessor<>();
		FileUtil.visitFiles(myPluginChannelDirectory, processor);

		long time = System.currentTimeMillis();
		processor.getResults().parallelStream().filter(file -> file.getName().endsWith("zip.json") || file.getName().endsWith("tar.gz.json")).forEach(this::processJsonFile);
		logger.info("Loading done by " + (System.currentTimeMillis() - time) + " ms. Channel: " + myChannel);
	}

	private void processJsonFile(File jsonFile)
	{
		String path = jsonFile.getPath();
		logger.info("Analyze: " + path);

		PluginNode pluginNode;
		try (FileReader fileReader = new FileReader(jsonFile))
		{
			pluginNode = GsonUtil.get().fromJson(fileReader, PluginNode.class);
		}
		catch(IOException e)
		{
			logger.error(e.getMessage(), e);
			return;
		}

		pluginNode.clean();

		String name = jsonFile.getName();
		File targetArchive = new File(jsonFile.getParentFile(), name.substring(0, name.length() - 5));
		if(!targetArchive.exists())
		{
			jsonFile.delete();

			logger.warn("Zombie json file: " + path);
			return;
		}

		PluginsState pluginsState = myPlugins.computeIfAbsent(pluginNode.id, id -> new PluginsState(myPluginChannelDirectory, pluginNode.id));

		ReentrantReadWriteLock.WriteLock writeLock = pluginsState.myLock.writeLock();
		try
		{
			writeLock.lock();

			pluginNode.length = targetArchive.length();
			pluginNode.targetFile = targetArchive;

			pluginsState.add(pluginNode);
		}
		finally
		{
			writeLock.unlock();
		}
	}
}
