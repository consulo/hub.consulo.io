package consulo.hub.backend.repository;

import consulo.hub.shared.repository.PluginChannel;
import consulo.hub.shared.repository.PluginNode;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 18/05/2023
 */
public interface RepositoryNodeState
{
	@Nullable
	PluginNode select(@Nonnull String platformVersion, @Nullable String version, boolean platformBuildSelect);

	default void runOver(@Nonnull String platformVersion, @Nullable String version, boolean platformBuildSelect, Consumer<PluginNode> consumer)
	{
		PluginNode select = select(platformVersion, version, platformBuildSelect);
		if(select != null)
		{
			consumer.accept(select);
		}
	}

	void selectInto(@Nonnull PluginStatisticsService statisticsService, @Nonnull PluginChannel channel, @Nonnull String platformVersion, boolean platformBuildSelect, List<PluginNode> list);

	boolean isInRepository(String version, String platformVersion);

	void forEach(@Nonnull Consumer<PluginNode> consumer);

	void remove(String version, String platformVersion);
}
