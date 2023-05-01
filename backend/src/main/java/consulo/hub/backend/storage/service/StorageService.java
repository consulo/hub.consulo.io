package consulo.hub.backend.storage.service;

import consulo.hub.backend.storage.repository.StorageFileRepository;
import consulo.hub.backend.storage.repository.StoragePluginRepository;
import consulo.hub.shared.auth.domain.UserAccount;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author VISTALL
 * @since 20/08/2021
 */
@Service
@Transactional
public class StorageService
{
	@Autowired
	private StorageFileRepository myStorageFileRepository;

	@Autowired
	private StoragePluginRepository myStoragePluginRepository;

	public void wipeData(UserAccount userAccount)
	{
		myStorageFileRepository.deleteAllByUser(userAccount);

		myStoragePluginRepository.deleteAllByUser(userAccount);
	}
}
