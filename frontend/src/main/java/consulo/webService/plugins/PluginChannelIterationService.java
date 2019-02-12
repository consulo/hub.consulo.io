package consulo.webService.plugins;

import gnu.trove.THashSet;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.google.common.annotations.VisibleForTesting;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.util.ArrayUtil;
import consulo.webService.UserConfigurationService;
import consulo.webService.plugins.pluginsState.PluginsState;

/**
 * @author VISTALL
 * @since 03-Jan-17
 */
@Service
public class PluginChannelIterationService
{
	public static final String ourConsuloBootBuild = "1555";
	public static final int ourMaxBuildCount = 5;

	private static final Logger logger = LoggerFactory.getLogger(PluginChannelIterationService.class);

	private final UserConfigurationService myUserConfigurationService;

	private final PluginDeployService myPluginDeployService;

	@Autowired
	public PluginChannelIterationService(UserConfigurationService userConfigurationService, PluginDeployService pluginDeployService)
	{
		myUserConfigurationService = userConfigurationService;
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
		PluginChannelService pluginChannelService = myUserConfigurationService.getRepositoryByChannel(pluginChannel);
		if(pluginChannelService.isLoading())
		{
			return;
		}

		Set<String> outdatedPlatformVersions = new THashSet<>();
		List<PluginNode> toRemove = new ArrayList<>();

		Map<String, PluginsState> pluginStates = pluginChannelService.copyPluginsState();
		// first of all we need check platform nodes
		for(String platformPluginId : PluginChannelService.ourPlatformPluginIds)
		{
			PluginsState pluginsState = pluginStates.get(platformPluginId);
			if(pluginsState == null)
			{
				continue;
			}

			NavigableMap<String, NavigableSet<PluginNode>> map = pluginsState.getPluginsByPlatformVersion();

			int i = map.size();
			for(Map.Entry<String, NavigableSet<PluginNode>> entry : map.entrySet())
			{
				String platformVersion = entry.getKey();
				if(ourConsuloBootBuild.equals(platformVersion))
				{
					i--;
					continue;
				}

				if(i > ourMaxBuildCount)
				{
					outdatedPlatformVersions.add(platformVersion);
					NavigableSet<PluginNode> value = entry.getValue();
					if(!value.isEmpty())
					{
						toRemove.add(value.iterator().next());
					}

					i--;
				}
			}
		}

		// process other plugins
		for(Map.Entry<String, PluginsState> entry : pluginStates.entrySet())
		{
			if(ArrayUtil.contains(PluginChannelService.ourPlatformPluginIds, entry.getKey()))
			{
				continue;
			}

			PluginsState pluginsState = entry.getValue();

			NavigableMap<String, NavigableSet<PluginNode>> map = pluginsState.getPluginsByPlatformVersion();

			for(Map.Entry<String, NavigableSet<PluginNode>> platformVersionEntry : map.entrySet())
			{
				String platformVersion = platformVersionEntry.getKey();
				NavigableSet<PluginNode> pluginNodes = platformVersionEntry.getValue();

				// drop all plugins for outdated platfomrs
				if(outdatedPlatformVersions.contains(platformVersion))
				{
					toRemove.addAll(pluginNodes);
					continue;
				}

				int i = pluginNodes.size();
				for(PluginNode node : pluginNodes)
				{
					if(i > ourMaxBuildCount)
					{
						toRemove.add(node);
					}

					i--;
				}
			}
		}

		for(PluginNode node : toRemove)
		{
			logger.info("removing id=" + node.id + ", version=" + node.version + ", platformVersion=" + node.platformVersion + ", channel=" + pluginChannel);

			pluginChannelService.remove(node.id, node.version, node.platformVersion);
		}
	}

	private static boolean weNeedSkip(PluginNode pluginNode)
	{
		if(PluginChannelService.ourStandardWinId.equals(pluginNode.id) && pluginNode.version.equals(ourConsuloBootBuild))
		{
			return true;
		}
		if(PluginChannelService.isPlatformNode(pluginNode.id))
		{
			return false;
		}
		return Comparing.equal(pluginNode.platformVersion, ourConsuloBootBuild);
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
		PluginChannelService fromChannel = myUserConfigurationService.getRepositoryByChannel(from);
		PluginChannelService toChannel = myUserConfigurationService.getRepositoryByChannel(to);

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
				if(PluginChannelService.isPlatformNode(node.id))
				{
					// special windows archive will processed as while deploying tar.gz files
					if(node.id.endsWith("-zip"))
					{
						return;
					}

					myPluginDeployService.deployPlatform(to, Integer.parseInt(node.platformVersion), node.id, targetFile);
				}
				else
				{
					toChannel.push(node, "zip", file -> FileUtilRt.copy(targetFile, file));
				}
			}
			catch(Exception e)
			{
				logger.error("Problem with plugin node: " + originalNode.id + ":" + originalNode.version, e);
			}
		});
	}
}
