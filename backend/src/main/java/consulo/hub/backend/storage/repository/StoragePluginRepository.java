package consulo.hub.backend.storage.repository;

import consulo.hub.shared.auth.domain.UserAccount;
import consulo.hub.shared.storage.domain.StoragePlugin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author VISTALL
 * @since 13/09/2021
 */
public interface StoragePluginRepository extends JpaRepository<StoragePlugin, Long>
{
	List<StoragePlugin> findAllByUser(UserAccount user);

	StoragePlugin findByUserAndPluginId(UserAccount user, String pluginId);

	void deleteAllByUser(UserAccount user);
}
