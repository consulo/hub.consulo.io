package consulo.hub.backend.repository;

import consulo.hub.shared.repository.PluginChannel;
import consulo.hub.shared.repository.PluginNode;
import consulo.hub.shared.repository.util.RepositoryUtil;
import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 18/05/2023
 */
public interface RepositoryChannelsService
{
	@Nonnull
	RepositoryChannelStore getRepositoryByChannel(@Nonnull PluginChannel channel);

	@Nonnull
	default String getDeployPluginExtension()
	{
		return RepositoryChannelStore.PLUGIN_EXTENSION;
	}

	@Nonnull
	default String getNodeExtension(@Nonnull PluginNode pluginNode)
	{
		if(!RepositoryUtil.isPlatformNode(pluginNode.id))
		{
			return getDeployPluginExtension();
		}

		if(pluginNode.id.endsWith("-zip"))
		{
			return "zip";
		}

		return "tar";
	}
}
