package consulo.hub.backend.repository.impl.store;

import com.google.common.annotations.VisibleForTesting;
import consulo.hub.backend.repository.PluginStatisticsService;
import consulo.hub.backend.repository.RepositoryChannelStore;
import consulo.hub.backend.repository.RepositoryNodeState;
import consulo.hub.backend.util.AccessToken;
import consulo.hub.shared.repository.PluginChannel;
import consulo.hub.shared.repository.PluginNode;
import consulo.hub.shared.repository.util.RepositoryUtil;
import consulo.util.lang.Comparing;
import consulo.util.lang.VersionComparatorUtil;
import consulo.util.lang.function.ThrowableConsumer;
import consulo.util.lang.function.ThrowableFunction;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.*;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 18/05/2023
 */
public abstract class BaseRepositoryNodeState implements RepositoryNodeState
{
	private static final Logger LOG = LoggerFactory.getLogger(BaseRepositoryNodeState.class);

	protected final NavigableMap<String, NavigableSet<PluginNode>> myPluginsByPlatformVersion = new TreeMap<>();

	protected final String myPluginId;

	protected BaseRepositoryNodeState(String pluginId)
	{
		myPluginId = pluginId;
	}

	@Nonnull
	public NavigableMap<String, NavigableSet<PluginNode>> getPluginsByPlatformVersion()
	{
		return myPluginsByPlatformVersion;
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
		NavigableSet<PluginNode> nodes = myPluginsByPlatformVersion.computeIfAbsent(pluginNode.platformVersion, BaseRepositoryNodeState::newTreeSet);

		nodes.add(pluginNode);
	}

	@Nonnull
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

	@Override
	public void forEach(@Nonnull Consumer<PluginNode> consumer)
	{
		try (AccessToken ignored = readLock())
		{
			for(NavigableSet<PluginNode> pluginNodes : myPluginsByPlatformVersion.values())
			{
				pluginNodes.forEach(consumer);
			}
		}
	}

	@Override
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
				if(Objects.equals(version, node.version))
				{
					return true;
				}
			}

			return false;
		}
	}

	@Override
	@Nullable
	public PluginNode select(@Nonnull String platformVersion, @Nullable String version, boolean platformBuildSelect)
	{
		try (AccessToken ignored = readLock())
		{
			NavigableSet<PluginNode> pluginNodes = getPluginSetByVersion(platformVersion, platformBuildSelect);
			if(pluginNodes == null || pluginNodes.isEmpty())
			{
				return null;
			}

			if(version == null || RepositoryChannelStore.SNAPSHOT.equals(version))
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

	@Override
	public void selectInto(@Nonnull PluginStatisticsService statisticsService, @Nonnull PluginChannel channel, @Nonnull String platformVersion, boolean platformBuildSelect, List<PluginNode> list)
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
			lastCloned.downloads = statisticsService.getDownloadStatCount(last.id, channel);
			lastCloned.downloadsAll = statisticsService.getDownloadStatCountAll(last.id);
			list.add(lastCloned);
		}
	}


	@Override
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

				removeRepositoryArtifact(target);
			}
		}
	}

	public static void prepareNode(PluginNode pluginNode, Path nioPath)
	{
		pluginNode.checksum.md5 = calculateChecksum(nioPath, DigestUtils::md5Hex);
		pluginNode.checksum.sha_256 = calculateChecksum(nioPath, DigestUtils::sha256Hex);
		pluginNode.checksum.sha3_256 = calculateChecksum(nioPath, stream ->
		{
			MessageDigest digest = MessageDigest.getInstance("SHA3-256");
			DigestUtils.updateDigest(digest, stream);
			return Hex.encodeHexString(digest.digest());
		});

		pluginNode.date = System.currentTimeMillis();
	}

	public static String calculateChecksum(Path input, ThrowableFunction<InputStream, String, Exception> digFunc)
	{
		try (InputStream in = Files.newInputStream(input))
		{
			return digFunc.apply(in).toUpperCase(Locale.ROOT);
		}
		catch(Exception e)
		{
			LOG.error("Can't calculate checksum " + input, e);
			return "__error__";
		}
	}

	public abstract void push(PluginNode pluginNode, String ext, ThrowableConsumer<Path, Exception> writeConsumer) throws Exception;

	protected abstract void removeRepositoryArtifact(PluginNode target);

	@Nullable
	private NavigableSet<PluginNode> getPluginSetByVersion(@Nonnull String platformVersion, boolean platformBuildSelect)
	{
		NavigableMap<String, NavigableSet<PluginNode>> map = myPluginsByPlatformVersion;
		if(RepositoryChannelStore.SNAPSHOT.equals(platformVersion) || !platformBuildSelect && RepositoryUtil.isPlatformNode(myPluginId))
		{
			Map.Entry<String, NavigableSet<PluginNode>> entry = map.lastEntry();
			return entry == null ? null : entry.getValue();
		}
		return map.get(platformVersion);
	}

	private static NavigableSet<PluginNode> newTreeSet(@Nonnull String unused)
	{
		return new TreeSet<>((o1, o2) -> VersionComparatorUtil.compare(o1.version, o2.version));
	}
}
