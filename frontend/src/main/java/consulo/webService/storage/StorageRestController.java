package consulo.webService.storage;

import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.BufferExposingByteArrayOutputStream;
import consulo.externalStorage.storage.DataCompressor;
import consulo.webService.auth.oauth2.domain.OAuth2AuthenticationAccessToken;
import consulo.webService.auth.oauth2.mongo.OAuth2AccessTokenRepository;
import consulo.webService.storage.bean.InfoAllBeanResponse;
import consulo.webService.storage.bean.PushFileBeanRequest;
import consulo.webService.storage.bean.PushFileBeanResponse;
import consulo.webService.storage.mongo.MongoStorageFile;
import consulo.webService.storage.mongo.MongoStorageFileRepository;
import consulo.webService.storage.mongo.MongoStorageFileUpdateBy;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
	private MongoStorageFileRepository myStorageFileRepository;

	@Autowired
	private OAuth2AccessTokenRepository myOAuth2AccessTokenRepository;

	@RequestMapping(value = "/api/storage/infoAll", method = RequestMethod.GET)
	public InfoAllBeanResponse infoAll(@RequestHeader("Authorization") String authorization) throws IOException
	{
		OAuth2AuthenticationAccessToken token = myOAuth2AccessTokenRepository.findByTokenId(authorization);
		if(token == null)
		{
			throw new NotAuthorizedException();
		}

		InfoAllBeanResponse response = new InfoAllBeanResponse();

		List<MongoStorageFile> files = myStorageFileRepository.findByEmail(token.getUserName());
		for(MongoStorageFile file : files)
		{
			response.files.put(file.getFilePath(), file.getModCount());
		}

		return response;
	}

	@RequestMapping(value = "/api/storage/getAll", method = RequestMethod.GET)
	public ResponseEntity<?> getAll(@RequestHeader("Authorization") String authorization) throws IOException
	{
		OAuth2AuthenticationAccessToken token = myOAuth2AccessTokenRepository.findByTokenId(authorization);
		if(token == null)
		{
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		List<MongoStorageFile> list = myStorageFileRepository.findByEmail(token.getUserName());
		if(list.isEmpty())
		{
			return ResponseEntity.noContent().build();
		}

		BufferExposingByteArrayOutputStream outputStream = new BufferExposingByteArrayOutputStream();

		try(ZipArchiveOutputStream zipStream = new ZipArchiveOutputStream(outputStream))
		{
			zipStream.setMethod(ZipArchiveOutputStream.DEFLATED);
			zipStream.setEncoding(StandardCharsets.UTF_8.toString());

			for(MongoStorageFile storageFile : list)
			{
				String path = storageFile.getFilePath().replace("\\", "/");

				ZipArchiveEntry entry = new ZipArchiveEntry(path);

				zipStream.putArchiveEntry(entry);
				zipStream.write(storageFile.getData());
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
		OAuth2AuthenticationAccessToken token = myOAuth2AccessTokenRepository.findByTokenId(authorization);
		if(token == null)
		{
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		MongoStorageFile storageFile = myStorageFileRepository.findByEmailAndFilePath(token.getUserName(), filePath);
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
			byte[] data = storageFile.getData();
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
		OAuth2AuthenticationAccessToken token = myOAuth2AccessTokenRepository.findByTokenId(authorization);
		if(token == null)
		{
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		MongoStorageFile storageFile = myStorageFileRepository.findByEmailAndFilePath(token.getUserName(), filePath);
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
		OAuth2AuthenticationAccessToken token = myOAuth2AccessTokenRepository.findByTokenId(authorization);
		if(token == null)
		{
			throw new NotAuthorizedException();
		}

		String filePath = data.getFilePath();

		MongoStorageFile prevFile = myStorageFileRepository.findByEmailAndFilePath(token.getUserName(), filePath);

		int count;
		MongoStorageFile file = prevFile == null ? new MongoStorageFile() : prevFile;

		if(prevFile != null)
		{
			file.setModCount(count = (file.getModCount() + 1));
		}
		else
		{
			file.setFilePath(filePath);
			file.setEmail(token.getUserName());
			file.setModCount(count = 1);
		}

		MongoStorageFileUpdateBy by = new MongoStorageFileUpdateBy();
		by.setTime(System.currentTimeMillis());
		data.copyTo(by);

		file.setUpdateBy(by);

		String bytes = data.getBytes();

		Pair<byte[], Integer> uncompress = DataCompressor.uncompress(new ByteArrayInputStream(Base64.getDecoder().decode(bytes)));

		file.setData(uncompress.getFirst());

		myStorageFileRepository.save(file);

		return new PushFileBeanResponse(count);
	}
}
