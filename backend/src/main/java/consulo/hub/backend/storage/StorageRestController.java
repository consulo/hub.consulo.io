package consulo.hub.backend.storage;

import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.BufferExposingByteArrayOutputStream;
import consulo.externalStorage.storage.DataCompressor;
import consulo.hub.backend.auth.repository.UserAccountRepository;
import consulo.hub.backend.storage.bean.InfoAllBeanResponse;
import consulo.hub.backend.storage.bean.PushFileBeanRequest;
import consulo.hub.backend.storage.bean.PushFileBeanResponse;
import consulo.hub.backend.storage.repository.StorageFileRepository;
import consulo.hub.backend.storage.service.StorageService;
import consulo.hub.shared.auth.domain.UserAccount;
import consulo.hub.shared.storage.domain.StorageFile;
import consulo.hub.shared.storage.domain.StorageFileUpdateBy;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

/**
 * @author VISTALL
 * @since 12-Feb-17
 */
@RestController
public class StorageRestController
{
	@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
	private static class NotAuthorizedException extends RuntimeException
	{
	}

	@Autowired
	private StorageFileRepository myStorageFileRepository;

	@Autowired
	private UserAccountRepository myUserAccountRepository;

	@Autowired
	private StorageService myStorageService;

	@RequestMapping(value = "/api/storage/infoAll", method = RequestMethod.GET)
	public InfoAllBeanResponse infoAll(@AuthenticationPrincipal UserAccount account) throws IOException
	{
		InfoAllBeanResponse response = new InfoAllBeanResponse();

		List<StorageFile> files = myStorageFileRepository.findAllByUser(account);
		for(StorageFile file : files)
		{
			response.files.put(file.getFilePath(), file.getModCount());
		}

		return response;
	}

	@RequestMapping(value = "/api/storage/getAll", method = RequestMethod.GET)
	public ResponseEntity<?> getAll(@AuthenticationPrincipal UserAccount account) throws IOException
	{
		List<StorageFile> list = myStorageFileRepository.findAllByUser(account);
		if(list.isEmpty())
		{
			return ResponseEntity.noContent().build();
		}

		BufferExposingByteArrayOutputStream outputStream = new BufferExposingByteArrayOutputStream();

		try(ZipArchiveOutputStream zipStream = new ZipArchiveOutputStream(outputStream))
		{
			zipStream.setMethod(ZipArchiveOutputStream.DEFLATED);
			zipStream.setEncoding(StandardCharsets.UTF_8.toString());

			for(StorageFile storageFile : list)
			{
				String path = storageFile.getFilePath().replace("\\", "/");

				ZipArchiveEntry entry = new ZipArchiveEntry(path);

				zipStream.putArchiveEntry(entry);
				zipStream.write(storageFile.getFileData());
				zipStream.closeArchiveEntry();

				entry = new ZipArchiveEntry(path + ".modcount");
				zipStream.putArchiveEntry(entry);
				zipStream.write(String.valueOf(storageFile.getModCount()).getBytes(StandardCharsets.UTF_8));
				zipStream.closeArchiveEntry();
			}

			zipStream.finish();
		}

		byte[] bytes = outputStream.toByteArray();
		return ResponseEntity.status(HttpStatus.OK).contentLength(bytes.length).body(bytes);
	}

	@RequestMapping(value = "/api/storage/getFile", method = RequestMethod.GET)
	public ResponseEntity<?> getFile(@RequestParam("filePath") String filePath, @AuthenticationPrincipal UserAccount account)
	{
		StorageFile storageFile = myStorageFileRepository.findByUserAndFilePath(account, filePath);

		if(storageFile == null)
		{
			return ResponseEntity.notFound().build();
		}

		try
		{
			byte[] data = storageFile.getFileData();
			byte[] compressedData = DataCompressor.compress(data, storageFile.getModCount());
			return ResponseEntity.ok(new ByteArrayResource(compressedData));
		}
		catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	@RequestMapping(value = "/api/storage/deleteFile", method = RequestMethod.GET)
	public ResponseEntity<?> deleteFile(@RequestParam("filePath") String filePath, @AuthenticationPrincipal UserAccount account)
	{
		StorageFile storageFile = myStorageFileRepository.findByUserAndFilePath(account, filePath);
		if(storageFile == null)
		{
			return ResponseEntity.notFound().build();
		}

		myStorageFileRepository.delete(storageFile);
		return ResponseEntity.ok().build();
	}

	@RequestMapping(value = "/api/storage/pushFile", method = RequestMethod.POST)
	public PushFileBeanResponse pushFile(@RequestBody(required = true) PushFileBeanRequest data, @AuthenticationPrincipal UserAccount account) throws Exception
	{
		String filePath = data.getFilePath();

		StorageFile prevFile = myStorageFileRepository.findByUserAndFilePath(account, filePath);

		int count;
		StorageFile file = prevFile == null ? new StorageFile() : prevFile;

		if(prevFile != null)
		{
			file.setModCount(count = (file.getModCount() + 1));
		}
		else
		{
			file.setFilePath(filePath);
			file.setUser(account);
			file.setModCount(count = 1);
		}

		StorageFileUpdateBy by = new StorageFileUpdateBy();
		by.setTime(System.currentTimeMillis());
		data.copyTo(by);

		file.setUpdateBy(by);

		String bytes = data.getBytes();

		Pair<byte[], Integer> uncompress = DataCompressor.uncompress(new ByteArrayInputStream(Base64.getDecoder().decode(bytes)));

		file.setFileData(uncompress.getFirst());

		myStorageFileRepository.save(file);

		return new PushFileBeanResponse(count);
	}
}
