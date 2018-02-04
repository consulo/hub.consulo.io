package consulo.webService.plugins.pluginsState;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileSystemUtils;
import com.google.common.annotations.VisibleForTesting;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.util.ThrowableConsumer;
import com.intellij.util.text.VersionComparatorUtil;
import consulo.webService.plugins.PluginChannelService;
import consulo.webService.plugins.PluginNode;
import consulo.webService.plugins.PluginStatisticsService;
import consulo.webService.util.GsonUtil;

/**
 * @author VISTALL
 * @since 09-May-17
 */
public class PluginsState
{
	private static final Logger logger = LoggerFactory.getLogger(PluginChannelService.class);

	private final NavigableMap<String, NavigableSet<PluginNode>> myPluginsByPlatformVersion = new TreeMap<>();

	protected final File myPluginDirectory;

	private final String myPluginId;

	protected PluginsState(File rootDir, String pluginId)
	{
		myPluginId = pluginId;
		myPluginDirectory = new File(rootDir, pluginId);
	}

	@NotNull
	public NavigableMap<String, NavigableSet<PluginNode>> getPluginsByPlatformVersion()
	{
		return myPluginsByPlatformVersion;
	}

	/**
	 * @return lock free plugins state
	 */
	@NotNull
	public PluginsState copy()
	{
		PluginsState copy = new PluginsState(myPluginDirectory, myPluginId);

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

	protected AccessToken readLock()
	{
		return AccessToken.EMPTY_ACCESS_TOKEN;
	}

	protected AccessToken writeLock()
	{
		return AccessToken.EMPTY_ACCESS_TOKEN;
	}

	@VisibleForTesting
	public void _add(PluginNode pluginNode)
	{
		NavigableSet<PluginNode> nodes = myPluginsByPlatformVersion.computeIfAbsent(pluginNode.platformVersion, PluginsState::newTreeSet);

		nodes.add(pluginNode);
	}

	private static NavigableSet<PluginNode> newTreeSet(@NotNull String unused)
	{
		return new TreeSet<>((o1, o2) -> VersionComparatorUtil.compare(o1.version, o2.version));
	}

	@NotNull
	public List<PluginNode> getAll()
	{
		try (AccessToken ignored = readLock())
		{
			List<PluginNode> list = new ArrayList<>();
			for(NavigableSet<PluginNode> pluginNodes : myPluginsByPlatformVersion.values())
			{
				list.addAll(pluginNodes);
			}
			return list;
		}
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

	public boolean isInRepository(String version, String platformVersion)
	{
		try (AccessToken ignored = readLock())
		{
			NavigableSet<PluginNode> nodes = myPluginsByPlatformVersion.get(platformVersion);

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
	}

	public void remove(String version, String platformVersion)
	{
		try (AccessToken ignored = writeLock())
		{
			NavigableSet<PluginNode> nodes = myPluginsByPlatformVersion.get(platformVersion);

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

				if(nodes.isEmpty())
				{
					myPluginsByPlatformVersion.remove(platformVersion);
				}

				File targetFile = target.targetFile;
				// in tests target file is null
				if(targetFile != null)
				{
					targetFile.delete();

					File jsonFile = new File(targetFile.getParentFile(), targetFile.getName() + ".json");

					jsonFile.delete();
				}
			}
		}
	}

	@Nullable
	public PluginNode select(@NotNull String platformVersion, @Nullable String version, boolean platformBuildSelect)
	{
		try (AccessToken ignored = readLock())
		{
			NavigableSet<PluginNode> pluginNodes = getPluginSetByVersion(platformVersion, platformBuildSelect);
			if(pluginNodes == null || pluginNodes.isEmpty())
			{
				return null;
			}

			if(version == null || PluginChannelService.SNAPSHOT.equals(version))
			{
				return pluginNodes.last();
			}

			for(PluginNode pluginNode : pluginNodes)
			{
				if(Comparing.equal(pluginNode.version, version))
				{
					return pluginNode;
				}
			}

			return null;
		}
	}

	public void selectInto(@NotNull PluginStatisticsService statisticsService, @NotNull String platformVersion, boolean platformBuildSelect, List<PluginNode> list)
	{
		try (AccessToken ignored = readLock())
		{
			NavigableSet<PluginNode> pluginNodes = getPluginSetByVersion(platformVersion, platformBuildSelect);
			if(pluginNodes == null || pluginNodes.isEmpty())
			{
				return;
			}

			PluginNode last = pluginNodes.last();

			PluginNode lastCloned = last.clone();
			//lastCloned.downloads = statisticsService.getDownloadStat(last.id).size();
			list.add(lastCloned);
		}
	}

	public void push(PluginNode pluginNode, String ext, ThrowableConsumer<File, Exception> writeConsumer) throws Exception
	{
		try (AccessToken ignored = writeLock())
		{
			File fileForPlugin = getFileForPlugin(pluginNode.version, ext);

			writeConsumer.consume(fileForPlugin);

			pluginNode.date = System.currentTimeMillis();
			pluginNode.length = fileForPlugin.length();
			pluginNode.targetFile = fileForPlugin;
			pluginNode.clean();

			File metaFile = new File(fileForPlugin.getParentFile(), fileForPlugin.getName() + ".json");

			FileSystemUtils.deleteRecursively(metaFile);

			FileUtil.writeToFile(metaFile, GsonUtil.get().toJson(pluginNode));

			_add(pluginNode);
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

	@Nullable
	private NavigableSet<PluginNode> getPluginSetByVersion(@NotNull String platformVersion, boolean platformBuildSelect)
	{
		NavigableMap<String, NavigableSet<PluginNode>> map = myPluginsByPlatformVersion;
		if(PluginChannelService.SNAPSHOT.equals(platformVersion) || !platformBuildSelect && PluginChannelService.isPlatformNode(myPluginId))
		{
			Map.Entry<String, NavigableSet<PluginNode>> entry = map.lastEntry();
			return entry == null ? null : entry.getValue();
		}
		return map.get(platformVersion);
	}
}
