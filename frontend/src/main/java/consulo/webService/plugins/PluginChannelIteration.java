package consulo.webService.plugins;

import java.io.File;
import java.util.Arrays;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.intellij.openapi.util.io.FileUtilRt;
import consulo.webService.UserConfigurationService;

/**
 * @author VISTALL
 * @since 03-Jan-17
 */
@Component
public class PluginChannelIteration
{
	private static final Logger logger = LoggerFactory.getLogger(PluginChannelIteration.class);

	private final UserConfigurationService myUserConfigurationService;

	private final PluginDeployService myPluginDeployService;

	@Autowired
	public PluginChannelIteration(UserConfigurationService userConfigurationService, PluginDeployService pluginDeployService)
	{
		myUserConfigurationService = userConfigurationService;
		myPluginDeployService = pluginDeployService;
	}

	@Scheduled(cron = "0 0 * * * *")
	public void cleanup()
	{
		Arrays.stream(PluginChannel.values()).parallel().forEach(this::cleanup);
	}

	private void cleanup(PluginChannel pluginChannel)
	{
		PluginChannelService pluginChannelService = myUserConfigurationService.getRepositoryByChannel(pluginChannel);
	}

	/**
	 * every hour
	 */
	@Scheduled(cron = "0 0 * * * *")
	public void iterAlpha()
	{
		iterate(PluginChannel.nightly, PluginChannel.alpha);
	}

	/**
	 * every week
	 */
	@Scheduled(cron = "0 0 0 * * MON")
	public void iterBeta()
	{
		iterate(PluginChannel.alpha, PluginChannel.beta);
	}

	/**
	 * every month
	 */
	@Scheduled(cron = "0 0 0 1 * *")
	public void iterRelease()
	{
		iterate(PluginChannel.beta, PluginChannel.release);
	}

	public void iterate(@NotNull PluginChannel from, @NotNull PluginChannel to)
	{
		PluginChannelService fromChannel = myUserConfigurationService.getRepositoryByChannel(from);
		PluginChannelService toChannel = myUserConfigurationService.getRepositoryByChannel(to);

		fromChannel.iteratePluginNodes(originalNode -> {
			if(toChannel.isInRepository(originalNode.id, originalNode.version, originalNode.platformVersion))
			{
				return;
			}

			PluginNode node = originalNode.clone();
			try
			{
				File targetFile = originalNode.targetFile;

				assert targetFile != null;

				logger.info("iterate pluginId=" + node.id + ", version=" + node.version + ", platformVersion=" + node.platformVersion + ", from=" + from + ", to=" + to);

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
