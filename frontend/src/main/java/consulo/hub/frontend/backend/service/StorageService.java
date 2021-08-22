package consulo.hub.frontend.backend.service;

import consulo.hub.shared.storage.domain.StorageFile;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author VISTALL
 * @since 21/08/2021
 */
@Service
public class StorageService
{
	public List<StorageFile> findAllByUser(int userId)
	{
		throw new UnsupportedOperationException();
	}

	public void wipeData(int userId)
	{
		throw new UnsupportedOperationException();
	}

	public StorageFile findOne(int userId, int storageFileId)
	{
		throw new UnsupportedOperationException();
	}
}
