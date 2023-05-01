package consulo.hub.frontend.vflow.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import consulo.hub.frontend.vflow.backend.BackendRequestor;
import consulo.hub.shared.storage.domain.StorageFile;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author VISTALL
 * @since 21/08/2021
 */
@Service
public class BackendStorageService
{
	private static final Logger LOG = LoggerFactory.getLogger(BackendStorageService.class);

	@Autowired
	private BackendRequestor myBackendRequestor;

	@Nonnull
	public List<StorageFile> listAll(long userId)
	{
		try
		{
			return myBackendRequestor.runRequest("/storage/list", Map.of("userId", String.valueOf(userId)), new TypeReference<List<StorageFile>>()
			{
			}, List::of);
		}
		catch(Exception e)
		{
			LOG.error("Failed to list storage files: " + userId, e);
			return List.of();
		}
	}

	public Map<String, String> deleteAll(long userId)
	{
		try
		{
			return myBackendRequestor.runRequest("/storage/deleteAll", Map.of("userId", String.valueOf(userId)), new TypeReference<Map<String, String>>()
			{
			});
		}
		catch(Exception e)
		{
			LOG.error("Failed to deleteAll: " + userId, e);
			return Map.of();
		}
	}

	public StorageFile find(long userId, long storageFileId)
	{
		try
		{
			return myBackendRequestor.runRequest("/storage/get", Map.of("userId", String.valueOf(userId), "fileId", String.valueOf(storageFileId)), StorageFile.class);
		}
		catch(Exception e)
		{
			LOG.error("Failed to get storage file: " + userId + ", fileId: " + storageFileId, e);
			return null;
		}
	}
}
