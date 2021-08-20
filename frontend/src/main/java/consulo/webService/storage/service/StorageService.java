package consulo.webService.storage.service;

import consulo.webService.auth.domain.UserAccount;
import consulo.webService.storage.repository.StorageFileRepository;
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
