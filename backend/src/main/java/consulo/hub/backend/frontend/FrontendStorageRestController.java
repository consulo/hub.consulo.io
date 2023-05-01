package consulo.hub.backend.frontend;

import consulo.hub.backend.auth.service.UserAccountService;
import consulo.hub.backend.storage.repository.StorageFileRepository;
import consulo.hub.backend.storage.service.StorageService;
import consulo.hub.shared.auth.domain.UserAccount;
import consulo.hub.shared.storage.domain.StorageFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @author VISTALL
 * @since 08/09/2021
 */
@RestController
public class FrontendStorageRestController
{
	@Autowired
	private StorageFileRepository myStorageFileRepository;

	@Autowired
	private UserAccountService myUserAccountService;

	@Autowired
	private StorageService myStorageService;

	@RequestMapping("/api/private/storage/list")
	public List<StorageFile> storageFilesListAll(@RequestParam("userId") long userId)
	{
		UserAccount account = myUserAccountService.findUser(userId);
		if(account == null)
		{
			throw new IllegalArgumentException();
		}

		return myStorageFileRepository.findAllByUser(account);
	}

	@RequestMapping("/api/private/storage/get")
	public StorageFile storageFilesListAll(@RequestParam("userId") long userId, @RequestParam("fileId") long fileId)
	{
		UserAccount account = myUserAccountService.findUser(userId);
		if(account == null)
		{
			throw new IllegalArgumentException();
		}

		return myStorageFileRepository.findById(fileId).get();
	}

	@RequestMapping("/api/private/storage/deleteAll")
	public Map<String, String> deleteAllStorageFiles(@RequestParam("userId") long userId)
	{
		UserAccount account = myUserAccountService.findUser(userId);
		if(account == null)
		{
			throw new IllegalArgumentException();
		}

		myStorageService.wipeData(account);
		return Map.of();
	}
}
