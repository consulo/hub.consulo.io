package consulo.hub.backend.storage;

import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.BufferExposingByteArrayOutputStream;
import consulo.externalStorage.storage.DataCompressor;
import consulo.hub.shared.auth.domain.UserAccount;
import consulo.hub.backend.auth.oauth2.domain.OAuth2AuthenticationAccessToken;
import consulo.hub.backend.auth.oauth2.mongo.OAuth2AccessTokenRepository;
import consulo.hub.backend.auth.repository.UserAccountRepository;
import consulo.hub.backend.storage.bean.InfoAllBeanResponse;
import consulo.hub.backend.storage.bean.PushFileBeanRequest;
import consulo.hub.backend.storage.bean.PushFileBeanResponse;
import consulo.hub.shared.storage.domain.StorageFile;
import consulo.hub.shared.storage.domain.StorageFileUpdateBy;
import consulo.hub.backend.storage.repository.StorageFileRepository;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

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
	private OAuth2AccessTokenRepository myOAuth2AccessTokenRepository;

	@Autowired
	private UserAccountRepository myUserAccountRepository;

	@Nonnull
	private UserAccount findUserByToken(@Nonnull String authorization)  throws NotAuthorizedException
	{
		OAuth2AuthenticationAccessToken token = myOAuth2AccessTokenRepository.findByTokenId(authorization);
		if(token == null)
		{
			throw new NotAuthorizedException();
		}

		return Objects.requireNonNull(myUserAccountRepository.findByUsername(token.getUserName()));
	}

	@RequestMapping(value = "/api/storage/infoAll", method = RequestMethod.GET)
	public InfoAllBeanResponse infoAll(@RequestHeader("Authorization") String authorization) throws IOException
	{
		UserAccount account = findUserByToken(authorization);

		InfoAllBeanResponse response = new InfoAllBeanResponse();

		List<StorageFile> files = myStorageFileRepository.findAllByUser(account);
		for(StorageFile file : files)
		{
			response.files.put(file.getFilePath(), file.getModCount());
		}

		return response;
	}

	@RequestMapping(value = "/api/storage/getAll", method = RequestMethod.GET)
	public ResponseEntity<?> getAll(@RequestHeader("Authorization") String authorization) throws IOException
	{
		UserAccount account = findUserByToken(authorization);

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
	public ResponseEntity<?> getFile(@RequestParam("filePath") String filePath, @RequestParam("modCount") int modCount, @RequestHeader("Authorization") String authorization)
	{
		UserAccount account = findUserByToken(authorization);

		StorageFile storageFile = myStorageFileRepository.findByUserAndFilePath(account, filePath);

		if(storageFile == null)
		{
			return ResponseEntity.notFound().build();
		}

		if(storageFile.getModCount() == modCount)
		{
			return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
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
	public ResponseEntity<?> deleteFile(@RequestParam("filePath") String filePath, @RequestHeader("Authorization") String authorization)
	{
		UserAccount account = findUserByToken(authorization);

		StorageFile storageFile = myStorageFileRepository.findByUserAndFilePath(account, filePath);
		if(storageFile == null)
		{
			return ResponseEntity.notFound().build();
		}

		myStorageFileRepository.delete(storageFile);
		return ResponseEntity.ok().build();
	}

	@RequestMapping(value = "/api/storage/pushFile", method = RequestMethod.POST)
	public PushFileBeanResponse pushFile(@RequestBody(required = true) PushFileBeanRequest data, @RequestHeader("Authorization") String authorization) throws Exception
	{
		UserAccount account = findUserByToken(authorization);

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
