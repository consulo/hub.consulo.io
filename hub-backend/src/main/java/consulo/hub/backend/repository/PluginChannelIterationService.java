package consulo.hub.backend.repository;

import com.google.common.annotations.VisibleForTesting;
import consulo.hub.shared.repository.PluginChannel;
import consulo.hub.shared.repository.PluginNode;
import consulo.hub.shared.repository.util.RepositoryUtil;
import consulo.util.io.FilePermissionCopier;
import consulo.util.io.FileUtil;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Arrays;

/**
 * @author VISTALL
 * @since 03-Jan-17
 */
@Service
public class PluginChannelIterationService
{
	public static final int ourMaxBuildCount = 5;

	private static final Logger logger = LoggerFactory.getLogger(PluginChannelIterationService.class);

	private final RepositoryChannelsService myChannelsService;

	private final PluginDeployService myPluginDeployService;

	@Autowired
	public PluginChannelIterationService(RepositoryChannelsService channelsService, PluginDeployService pluginDeployService)
	{
		myChannelsService = channelsService;
		myPluginDeployService = pluginDeployService;
	}

	@Scheduled(cron = "0 * * * * *")
	public void cleanup()
	{
		Arrays.stream(PluginChannel.values()).parallel().forEach(this::cleanup);
	}

	@VisibleForTesting
	public void cleanup(PluginChannel pluginChannel)
	{
//		PluginChannelService pluginChannelService = myPluginChannelsService.getRepositoryByChannel(pluginChannel);
//		if(pluginChannelService.isLoading())
//		{
//			return;
//		}
//
//		Set<String> outdatedPlatformVersions = new HashSet<>();
//		List<PluginNode> toRemove = new ArrayList<>();
//
//		Map<String, PluginsStateOld> pluginStates = pluginChannelService.copyPluginsState();
//		// first of all we need check platform nodes
//		for(String platformPluginId : RepositoryUtil.ourPlatformPluginIds)
//		{
//			PluginsStateOld pluginsState = pluginStates.get(platformPluginId);
//			if(pluginsState == null)
//			{
//				continue;
//			}
//
//			NavigableMap<String, NavigableSet<PluginNode>> map = pluginsState.getPluginsByPlatformVersion();
//
//			int i = map.size();
//			for(Map.Entry<String, NavigableSet<PluginNode>> entry : map.entrySet())
//			{
//				String platformVersion = entry.getKey();
//
//				if(i > ourMaxBuildCount)
//				{
//					outdatedPlatformVersions.add(platformVersion);
//					NavigableSet<PluginNode> value = entry.getValue();
//					if(!value.isEmpty())
//					{
//						toRemove.add(value.iterator().next());
//					}
//
//					i--;
//				}
//			}
//		}
//
//		// process other plugins
//		for(Map.Entry<String, PluginsStateOld> entry : pluginStates.entrySet())
//		{
//			if(ArrayUtil.contains(RepositoryUtil.ourPlatformPluginIds, entry.getKey()))
//			{
//				continue;
//			}
//
//			PluginsStateOld pluginsState = entry.getValue();
//
//			NavigableMap<String, NavigableSet<PluginNode>> map = pluginsState.getPluginsByPlatformVersion();
//
//			for(Map.Entry<String, NavigableSet<PluginNode>> platformVersionEntry : map.entrySet())
//			{
//				String platformVersion = platformVersionEntry.getKey();
//				NavigableSet<PluginNode> pluginNodes = platformVersionEntry.getValue();
//
//				// drop all plugins for outdated platfomrs
//				if(outdatedPlatformVersions.contains(platformVersion))
//				{
//					toRemove.addAll(pluginNodes);
//					continue;
//				}
//
//				int i = pluginNodes.size();
//				for(PluginNode node : pluginNodes)
//				{
//					if(i > ourMaxBuildCount)
//					{
//						toRemove.add(node);
//					}
//
//					i--;
//				}
//			}
//		}
//
//		for(PluginNode node : toRemove)
//		{
//			logger.info("removing id=" + node.id + ", version=" + node.version + ", platformVersion=" + node.platformVersion + ", channel=" + pluginChannel);
//
//			pluginChannelService.remove(node.id, node.version, node.platformVersion);
//		}
	}

	/**
	 * every hour
	 */
	//@Scheduled(cron = "0 0 * * * *")
	public void iterAlpha()
	{
		iterate(PluginChannel.nightly, PluginChannel.alpha);
	}

	/**
	 * every day
	 */
	@Scheduled(cron = "0 0 0 * * *")
	public void iterBeta()
	{
		iterate(PluginChannel.alpha, PluginChannel.beta);
	}

	/**
	 * every week
	 */
	@Scheduled(cron = "0 0 0 * * MON")
	public void iterRelease()
	{
		iterate(PluginChannel.beta, PluginChannel.release);
	}

	public void iterate(@Nonnull PluginChannel from, @Nonnull PluginChannel to)
	{
		RepositoryChannelStore fromChannel = myChannelsService.getRepositoryByChannel(from);
		RepositoryChannelStore toChannel = myChannelsService.getRepositoryByChannel(to);

		fromChannel.iteratePluginNodes(originalNode ->
		{
			if(toChannel.isInRepository(originalNode.id, originalNode.version, originalNode.platformVersion))
			{
				return;
			}

			PluginNode node = originalNode.clone();
			try
			{
				File targetFile = originalNode.targetFile;

				assert targetFile != null;

				logger.info("iterate id=" + node.id + ", version=" + node.version + ", platformVersion=" + node.platformVersion + ", from=" + from + ", to=" + to);

				// platform nodes have special logic
				if(RepositoryUtil.isPlatformNode(node.id))
				{
					myPluginDeployService.deployPlatform(to, Integer.parseInt(node.platformVersion), node.id, targetFile.toPath());
				}
				else
				{
					toChannel.push(node, myChannelsService.getDeployPluginExtension(), file -> FileUtil.copy(targetFile, file.toFile(), FilePermissionCopier.BY_NIO2));
				}
			}
			catch(Exception e)
			{
				logger.error("Problem with plugin node: " + originalNode.id + ":" + originalNode.version, e);
			}
		});
	}
}
