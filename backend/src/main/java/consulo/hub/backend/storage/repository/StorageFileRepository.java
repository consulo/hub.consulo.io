package consulo.hub.backend.storage.repository;

import consulo.hub.shared.auth.domain.UserAccount;
import consulo.hub.shared.storage.domain.StorageFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author VISTALL
 * @since 20/08/2021
 */
public interface StorageFileRepository extends JpaRepository<StorageFile, Integer>
{
	List<StorageFile> findAllByUser(UserAccount user);

	StorageFile findByUserAndFilePath(UserAccount user, String filePath);

	void deleteAllByUser(UserAccount user);
}
