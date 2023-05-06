package consulo.hub.backend.repository.repository;

import consulo.hub.shared.repository.PluginHistoryEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author VISTALL
 * @since 05/11/2021
 */
public interface PluginHistoryEntryRepository extends JpaRepository<PluginHistoryEntry, Long>
{
	List<PluginHistoryEntry> findAllByPluginIdAndPluginVersion(String pluginId, String pluginVersion);

	List<PluginHistoryEntry> findAllByPluginIdAndPluginVersionIn(String pluginId, List<String> pluginVersions);
}
