package consulo.hub.backend.repository;

import consulo.hub.shared.repository.PluginNode;
import consulo.util.lang.function.ThrowableConsumer;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 18/05/2023
 */
public interface RepositoryChannelStore
{
	String PLUGIN_EXTENSION = "consulo-plugin";

	String SNAPSHOT = "SNAPSHOT";

	void iteratePluginNodes(@Nonnull Consumer<PluginNode> consumer);

	@Nullable
	PluginNode select(@Nonnull String platformVersion, @Nonnull String pluginId, @Nullable String version, boolean platformBuildSelect);

	@Nonnull
	PluginNode[] select(@Nonnull PluginStatisticsService statisticsService, @Nonnull String platformVersion, boolean platformBuildSelect);

	void push(PluginNode pluginNode, String ext, ThrowableConsumer<Path, Exception> writeConsumer) throws Exception;

	@Nullable
	RepositoryNodeState getState(String pluginId);

	default boolean isInRepository(String pluginId, String version, String platformVersion)
	{
		RepositoryNodeState state = getState(pluginId);
		return state != null && state.isInRepository(version, platformVersion);
	}

	void remove(String pluginId, String version, String platformVersion);

	boolean isLoading();

	default void attachDownloadUrl(PluginNode pluginNode, String downloadUrl) throws IOException
	{
	}
}
