package consulo.hub.frontend.vflow.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import consulo.procoeton.core.backend.ApiBackendRequestor;
import consulo.hub.shared.storage.domain.StorageFile;
import consulo.procoeton.core.backend.BackendApiUrl;
import consulo.procoeton.core.backend.BackendServiceDownException;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author VISTALL
 * @since 2021-08-21
 */
@Service
public class BackendStorageService {
    private static final Logger LOG = LoggerFactory.getLogger(BackendStorageService.class);

    @Autowired
    private ApiBackendRequestor myApiBackendRequestor;

    @Nonnull
    public List<StorageFile> listAll(long userId) {
        try {
            return myApiBackendRequestor.runRequest(
                BackendApiUrl.toPrivate("/storage/list"),
                Map.of("userId", String.valueOf(userId)),
                new TypeReference<List<StorageFile>>() {
                },
                List::of
            );
        }
        catch (BackendServiceDownException e) {
            throw e;
        }
        catch (Exception e) {
            LOG.error("Failed to list storage files: " + userId, e);
            return List.of();
        }
    }

    public Map<String, String> deleteAll(long userId) {
        try {
            return myApiBackendRequestor.runRequest(
                BackendApiUrl.toPrivate("/storage/deleteAll"),
                Map.of("userId", String.valueOf(userId)),
                new TypeReference<Map<String, String>>() {
                }
            );
        }
        catch (BackendServiceDownException e) {
            throw e;
        }
        catch (Exception e) {
            LOG.error("Failed to deleteAll: " + userId, e);
            return Map.of();
        }
    }

    public StorageFile find(long userId, long storageFileId) {
        try {
            return myApiBackendRequestor.runRequest(
                BackendApiUrl.toPrivate("/storage/get"),
                Map.of("userId", String.valueOf(userId), "fileId", String.valueOf(storageFileId)),
                StorageFile.class
            );
        }
        catch (BackendServiceDownException e) {
            throw e;
        }
        catch (Exception e) {
            LOG.error("Failed to get storage file: " + userId + ", fileId: " + storageFileId, e);
            return null;
        }
    }
}
