package consulo.hub.backend.repository;

import com.google.common.annotations.VisibleForTesting;
import consulo.hub.backend.repository.impl.store.old.PluginsSetWithLock;
import consulo.hub.backend.repository.impl.store.old.PluginsState;
import consulo.hub.backend.util.GsonUtil;
import consulo.hub.shared.repository.PluginChannel;
import consulo.hub.shared.repository.PluginNode;
import consulo.hub.shared.repository.util.RepositoryUtil;
import consulo.util.collection.Lists;
import consulo.util.io.FileUtil;
import consulo.util.lang.Pair;
import consulo.util.lang.function.ThrowableConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 28-Aug-16
 */
public class PluginChannelService
{
	@Deprecated
	public static boolean isPlatformNode(String pluginId)
	{
		return RepositoryUtil.isPlatformNode(pluginId);
	}

	private static final Logger logger = LoggerFactory.getLogger(PluginChannelService.class);
	public static final String SNAPSHOT = "SNAPSHOT";

	private File myPluginChannelDirectory;

	private final PluginChannel myChannel;

	private final Map<String, PluginsState> myPlugins = new ConcurrentSkipListMap<>();

	private boolean myLoading;

	public PluginChannelService(PluginChannel channel)
	{
		myChannel = channel;
	}

	public boolean isInRepository(String pluginId, String version, String platformVersion)
	{
		PluginsState state = myPlugins.get(pluginId);
		return state != null && state.isInRepository(version, platformVersion);
	}

	@Nullable
	public PluginsState getState(String pluginId)
	{
		return myPlugins.get(pluginId);
	}

	public void remove(String pluginId, String version, String platformVersion)
	{
		PluginsState state = myPlugins.get(pluginId);
		if(state == null)
		{
			return;
		}

		state.remove(version, platformVersion);
	}

	@Nullable
	public PluginNode select(@Nonnull String platformVersion, @Nonnull String pluginId, @Nullable String version, boolean platformBuildSelect)
	{
		PluginsState state = myPlugins.get(pluginId);
		if(state == null)
		{
			return null;
		}

		return state.select(platformVersion, version, platformBuildSelect);
	}

	@Nonnull
	public PluginNode[] select(@Nonnull PluginStatisticsService statisticsService, @Nonnull String platformVersion, boolean platformBuildSelect)
	{
		List<PluginNode> list = new ArrayList<>();
		for(PluginsState state : myPlugins.values())
		{
			state.selectInto(statisticsService, myChannel, platformVersion, platformBuildSelect, list);
		}
		return list.isEmpty() ? PluginNode.EMPTY_ARRAY : list.toArray(new PluginNode[list.size()]);
	}

	@Nonnull
	public Map<String, PluginsState> copyPluginsState()
	{
		Map<String, PluginsState> map = new LinkedHashMap<>();
		for(Map.Entry<String, PluginsState> entry : myPlugins.entrySet())
		{
			map.put(entry.getKey(), entry.getValue().copy());
		}
		return map;
	}

	public void iteratePluginNodes(@Nonnull Consumer<PluginNode> consumer)
	{
		for(PluginsState pluginsState : myPlugins.values())
		{
			pluginsState.forEach(consumer);
		}
	}

	public void push(PluginNode pluginNode, String ext, ThrowableConsumer<File, Exception> writeConsumer) throws Exception
	{
		PluginsState pluginsState = myPlugins.computeIfAbsent(pluginNode.id, id -> new PluginsSetWithLock(getPluginChannelDirectory(), pluginNode.id));

		pluginsState.push(pluginNode, ext, writeConsumer);
	}

	@VisibleForTesting
	public void _add(PluginNode node) throws Exception
	{
		PluginsState pluginsState = myPlugins.computeIfAbsent(node.id, id -> new PluginsSetWithLock(getPluginChannelDirectory(), node.id));

		pluginsState._add(node);
	}

	public void initImpl(File pluginChannelDir)
	{
		logger.info("Starting initializing repository. Channel: " + myChannel);

		myLoading = true;
		File channelDir = new File(pluginChannelDir, myChannel.name());

		FileUtil.createDirectory(channelDir);

		myPluginChannelDirectory = channelDir;

		File[] pluginIdDirectories = myPluginChannelDirectory.listFiles();
		if(pluginIdDirectories == null)
		{
			logger.info("Loading empty. Channel: " + myChannel);
			return;
		}

		long time = System.currentTimeMillis();
		Map<String, List<Pair<PluginNode, File>>> map = new ConcurrentHashMap<>();

		Arrays.stream(pluginIdDirectories).parallel().filter(File::isDirectory).forEach(file ->
		{
			File[] files = file.listFiles();
			if(files != null)
			{
				Arrays.stream(files).parallel().filter(child -> child.getName().endsWith("zip.json") || child.getName().endsWith("tar.gz.json")).forEach(t -> processJsonFile(t, map));
			}
		});

		map.entrySet().parallelStream().forEach(this::processEntry);

		myLoading = false;
		logger.info("Loading done by " + (System.currentTimeMillis() - time) + " ms. Channel: " + myChannel);
	}

	public boolean isLoading()
	{
		return myLoading;
	}

	private void processJsonFile(File jsonFile, Map<String, List<Pair<PluginNode, File>>> map)
	{
		String path = jsonFile.getPath();

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

		List<Pair<PluginNode, File>> list = map.computeIfAbsent(pluginNode.id, it -> Lists.newLockFreeCopyOnWriteList());
		list.add(Pair.create(pluginNode, targetArchive));
	}

	private void processEntry(Map.Entry<String, List<Pair<PluginNode, File>>> entry)
	{
		String pluginId = entry.getKey();

		PluginsState pluginsState = myPlugins.computeIfAbsent(pluginId, id -> new PluginsSetWithLock(getPluginChannelDirectory(), pluginId));

		pluginsState.processEntry(entry);
	}

	@Nonnull
	private File getPluginChannelDirectory()
	{
		return Objects.requireNonNull(myPluginChannelDirectory);
	}
}
