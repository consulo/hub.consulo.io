package consulo.webService.plugins;

import com.google.common.annotations.VisibleForTesting;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ThrowableConsumer;
import com.intellij.util.containers.ContainerUtil;
import consulo.webService.plugins.pluginsState.PluginsSetWithLock;
import consulo.webService.plugins.pluginsState.PluginsState;
import consulo.webService.util.GsonUtil;
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
	public static final String ourStandardWinId = "consulo-win-no-jre";
	public static final String ourStandardLinuxId = "consulo-linux-no-jre";
	public static final String ourStandardMacId = "consulo-mac-no-jre";

	public static final String[] ourPlatformPluginIds = {
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
			List<PluginNode> nodes = pluginsState.getAll();
			nodes.forEach(consumer);
		}
	}

	public void push(PluginNode pluginNode, String ext, ThrowableConsumer<File, Exception> writeConsumer) throws Exception
	{
		PluginsState pluginsState = myPlugins.computeIfAbsent(pluginNode.id, id -> new PluginsSetWithLock(myPluginChannelDirectory, pluginNode.id));

		pluginsState.push(pluginNode, ext, writeConsumer);
	}

	@VisibleForTesting
	public void _add(PluginNode node) throws Exception
	{
		PluginsState pluginsState = myPlugins.computeIfAbsent(node.id, id -> new PluginsSetWithLock(myPluginChannelDirectory, node.id));

		pluginsState._add(node);
	}

	public void initImpl(File pluginChannelDir)
	{
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

		List<Pair<PluginNode, File>> list = map.computeIfAbsent(pluginNode.id, it -> ContainerUtil.createConcurrentList());
		list.add(Pair.create(pluginNode, targetArchive));
	}

	private void processEntry(Map.Entry<String, List<Pair<PluginNode, File>>> entry)
	{
		String pluginId = entry.getKey();

		PluginsState pluginsState = myPlugins.computeIfAbsent(pluginId, id -> new PluginsSetWithLock(myPluginChannelDirectory, pluginId));

		pluginsState.processEntry(entry);
	}
}
