package consulo.hub.backend.repository;

import consulo.hub.shared.repository.PluginNode;
import org.springframework.lang.NonNull;

import java.util.stream.Stream;

/**
 * @author VISTALL
 * @since 05/11/2021
 */
public interface PluginHistoryService
{
	void insert(RestPluginHistoryEntry[] historyEntries, PluginNode pluginNode);

	@NonNull
	Stream<RestPluginHistoryEntry> listPluginHistory(String pluginId, String pluginVersion);

	@NonNull
	Stream<RestPluginHistoryEntry> listPluginHistoryByRange(String pluginId,
															String fromVer,
															String toVer,
															boolean includeFromVersion);
}
