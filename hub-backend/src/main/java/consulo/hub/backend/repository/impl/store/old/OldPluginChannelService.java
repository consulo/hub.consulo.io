package consulo.hub.backend.repository.impl.store.old;

import consulo.hub.backend.repository.RepositoryChannelStore;
import consulo.hub.backend.repository.impl.store.BaseRepositoryChannelStore;
import consulo.hub.backend.util.GsonUtil;
import consulo.hub.shared.repository.PluginChannel;
import consulo.hub.shared.repository.PluginNode;
import consulo.util.collection.Lists;
import consulo.util.io.FileUtil;
import consulo.util.lang.Pair;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author VISTALL
 * @since 28-Aug-16
 */
@Deprecated
public class OldPluginChannelService extends BaseRepositoryChannelStore<OldPluginsState> implements RepositoryChannelStore
{
	private static final Logger logger = LoggerFactory.getLogger(OldPluginChannelService.class);

	private File myPluginChannelDirectory;

	protected boolean myLoading;

	public OldPluginChannelService(PluginChannel channel)
	{
		super(channel);
	}

	@Nonnull
	public Map<String, OldPluginsState> copyPluginsState()
	{
		Map<String, OldPluginsState> map = new LinkedHashMap<>();
		for(Map.Entry<String, OldPluginsState> entry : myPlugins.entrySet())
		{
			map.put(entry.getKey(), entry.getValue().copy());
		}
		return map;
	}

	@Override
	public boolean isLoading()
	{
		return myLoading;
	}

	@Override
	protected OldPluginsState creatRepositoryNodeState(String pluginId)
	{
		return new OldPluginsSetWithLock(getPluginChannelDirectory(), pluginId);
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

		pluginNode.cleanUp();

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

		OldPluginsState pluginsState = myPlugins.computeIfAbsent(pluginId, id -> new OldPluginsSetWithLock(getPluginChannelDirectory(), pluginId));

		pluginsState.processEntry(entry);
	}

	@Nonnull
	private File getPluginChannelDirectory()
	{
		return Objects.requireNonNull(myPluginChannelDirectory);
	}
}
