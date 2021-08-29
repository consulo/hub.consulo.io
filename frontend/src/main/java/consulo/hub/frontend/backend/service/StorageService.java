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
	public List<StorageFile> findAllByUser(long userId)
	{
		throw new UnsupportedOperationException();
	}

	public void wipeData(long userId)
	{
		throw new UnsupportedOperationException();
	}

	public StorageFile findOne(long userId, long storageFileId)
	{
		throw new UnsupportedOperationException();
	}
}
