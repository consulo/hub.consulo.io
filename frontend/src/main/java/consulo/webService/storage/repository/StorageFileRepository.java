package consulo.webService.storage.repository;

import consulo.webService.auth.domain.UserAccount;
import consulo.webService.storage.domain.StorageFile;
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
