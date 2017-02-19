package consulo.webService.storage;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.intellij.openapi.util.io.BufferExposingByteArrayOutputStream;
import consulo.webService.auth.oauth2.domain.OAuth2AuthenticationAccessToken;
import consulo.webService.auth.oauth2.mongo.OAuth2AccessTokenRepository;
import consulo.webService.storage.mongo.MongoStorageFile;
import consulo.webService.storage.mongo.MongoStorageFileRepository;

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

	public static class PushFile
	{
		public int modCount;

		public PushFile(int modCount)
		{
			this.modCount = modCount;
		}
	}

	@Autowired
	private MongoStorageFileRepository myStorageFileRepository;

	@Autowired
	private OAuth2AccessTokenRepository myOAuth2AccessTokenRepository;

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

		BufferExposingByteArrayOutputStream arrayOutputStream = new BufferExposingByteArrayOutputStream();
		try (DataOutputStream stream = new DataOutputStream(arrayOutputStream))
		{
			stream.writeByte(0x01); // version

			stream.writeShort(list.size());
			for(MongoStorageFile storageFile : list)
			{
				stream.writeInt(storageFile.getModCount());
				stream.writeUTF(storageFile.getFilePath());
				byte[] data = storageFile.getData();
				stream.writeShort(data.length);
				stream.write(data);
			}
		}

		return ResponseEntity.ok(new ByteArrayResource(arrayOutputStream.toByteArray()));
	}

	@RequestMapping(value = "/api/storage/getFile", method = RequestMethod.GET)
	public ResponseEntity<?> getFile(@RequestParam("filePath") String filePath, @RequestHeader("Authorization") String authorization)
	{
		OAuth2AuthenticationAccessToken token = myOAuth2AccessTokenRepository.findByTokenId(authorization);
		if(token == null)
		{
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		MongoStorageFile storageFile = myStorageFileRepository.findByEmailAndFilePath(token.getUserName(), filePath);
		if(storageFile == null)
		{
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(new ByteArrayResource(storageFile.getData()));
	}

	@RequestMapping(value = "/api/storage/pushFile", method = RequestMethod.POST)
	public PushFile pushFile(@RequestParam("filePath") String filePath, @RequestBody(required = true) byte[] data, @RequestHeader("Authorization") String authorization) throws Exception
	{
		OAuth2AuthenticationAccessToken token = myOAuth2AccessTokenRepository.findByTokenId(authorization);
		if(token == null)
		{
			throw new NotAuthorizedException();
		}

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

		file.setData(data);

		myStorageFileRepository.save(file);

		return new PushFile(count);
	}

}
