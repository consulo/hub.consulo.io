package consulo.hub.backend.repository.impl.store.old;

import consulo.hub.backend.repository.PluginDeployService;
import consulo.hub.backend.repository.RepositoryChannelIterationService;
import consulo.hub.backend.repository.RepositoryChannelStore;
import consulo.hub.backend.repository.RepositoryChannelsService;
import consulo.hub.shared.repository.PluginChannel;
import consulo.hub.shared.repository.PluginNode;
import consulo.hub.shared.repository.util.RepositoryUtil;
import consulo.util.io.FilePermissionCopier;
import consulo.util.io.FileUtil;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;

/**
 * @author VISTALL
 * @since 23/05/2023
 */
@Deprecated
public class OldRepositoryChannelIterationService implements RepositoryChannelIterationService
{
	private static final Logger logger = LoggerFactory.getLogger(OldRepositoryChannelIterationService.class);

	private final RepositoryChannelsService myChannelsService;

	private final PluginDeployService myPluginDeployService;

	@Autowired
	public OldRepositoryChannelIterationService(RepositoryChannelsService channelsService, PluginDeployService pluginDeployService)
	{
		myChannelsService = channelsService;
		myPluginDeployService = pluginDeployService;
	}

	@Override
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
					myPluginDeployService.deployPlatform(to, null, Integer.parseInt(node.platformVersion), node.id, targetFile.toPath());
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
