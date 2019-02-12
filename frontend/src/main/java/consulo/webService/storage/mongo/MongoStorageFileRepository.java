package consulo.webService.storage.mongo;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author VISTALL
 * @since 18-Feb-17
 */
public interface MongoStorageFileRepository extends MongoRepository<MongoStorageFile, String>
{
	@Nullable
	MongoStorageFile findByEmailAndFilePath(String email, String filePath);

	@Nonnull
	List<MongoStorageFile> findByEmail(String email);
}
