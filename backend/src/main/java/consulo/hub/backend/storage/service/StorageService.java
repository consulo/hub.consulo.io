package consulo.hub.backend.storage.service;

import consulo.hub.shared.auth.domain.UserAccount;
import consulo.hub.backend.storage.repository.StorageFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

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

	public void wipeData(UserAccount userAccount)
	{
		myStorageFileRepository.deleteAllByUser(userAccount);
	}
}
