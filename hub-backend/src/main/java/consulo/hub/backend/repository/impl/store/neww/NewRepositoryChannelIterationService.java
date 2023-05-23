package consulo.hub.backend.repository.impl.store.neww;

import consulo.hub.backend.repository.RepositoryChannelIterationService;
import consulo.hub.backend.repository.RepositoryChannelStore;
import consulo.hub.shared.repository.PluginChannel;
import consulo.hub.shared.repository.PluginNode;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author VISTALL
 * @since 23/05/2023
 */
@Service
public class NewRepositoryChannelIterationService implements RepositoryChannelIterationService
{
	private static final Logger logger = LoggerFactory.getLogger(NewRepositoryChannelIterationService.class);

	private final NewRepositoryChannelsService myRepositoryChannelsService;

	@Autowired
	public NewRepositoryChannelIterationService(NewRepositoryChannelsService repositoryChannelsService)
	{
		myRepositoryChannelsService = repositoryChannelsService;
	}

	@Override
	public void iterate(@Nonnull PluginChannel from, @Nonnull PluginChannel to)
	{
		NewInlineRepositoryStore inlineRepositoryStore = myRepositoryChannelsService.getInlineRepositoryStore();

		if(inlineRepositoryStore.isLoading())
		{
			logger.warn("NewInlineRepositoryStore busy - interation skipped");
			return;
		}

		RepositoryChannelStore fromChannel = myRepositoryChannelsService.getRepositoryByChannel(from);
		NewRepositoryChannelStore toChannel = (NewRepositoryChannelStore) myRepositoryChannelsService.getRepositoryByChannel(to);

		fromChannel.iteratePluginNodes(originalNode ->
		{
			if(toChannel.isInRepository(originalNode.id, originalNode.version, originalNode.platformVersion))
			{
				return;
			}

			String ext = myRepositoryChannelsService.getNodeExtension(originalNode);

			try
			{
				// update offline meta - add new channel
				inlineRepositoryStore.updateMeta(originalNode.id, originalNode.version, ext, meta ->
				{
					meta.channels.add(to);
				});

				// update online data
				PluginNode newArtifact = originalNode.clone();
				newArtifact.targetPath = originalNode.targetPath;
				assert newArtifact.targetPath != null;

				toChannel._add(newArtifact);

				logger.info("iterate id=" + newArtifact.id + ", version=" + newArtifact.version + ", platformVersion=" + newArtifact.platformVersion + ", from=" + from + ", to=" + to);
			}
			catch(Exception e)
			{
				logger.error("Problem with plugin node: " + originalNode.id + ":" + originalNode.version, e);
			}
		});
	}
}
