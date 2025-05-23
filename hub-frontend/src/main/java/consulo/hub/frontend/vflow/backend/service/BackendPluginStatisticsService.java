package consulo.hub.frontend.vflow.backend.service;

import consulo.hub.shared.repository.domain.RepositoryDownloadInfo;
import consulo.procoeton.core.backend.ApiBackendRequestor;
import consulo.procoeton.core.backend.BackendApiUrl;
import consulo.procoeton.core.backend.BackendServiceDownException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author VISTALL
 * @since 2021-08-21
 */
@Service
public class BackendPluginStatisticsService {
    private static final Logger LOG = LoggerFactory.getLogger(BackendPluginStatisticsService.class);

    @Autowired
    private ApiBackendRequestor myApiBackendRequestor;

    public RepositoryDownloadInfo[] getDownloadStat(String pluginId) {
        try {
            RepositoryDownloadInfo[] pluginIds = myApiBackendRequestor.runRequest(
                BackendApiUrl.toPrivate("/repository/downloadStat"),
                Map.of("pluginId", pluginId),
                RepositoryDownloadInfo[].class
            );
            if (pluginIds == null) {
                pluginIds = new RepositoryDownloadInfo[0];
            }
            return pluginIds;
        }
        catch (BackendServiceDownException e) {
            throw e;
        }
        catch (Exception e) {
            LOG.warn("Fail to get download stat: " + pluginId, e);
            return new RepositoryDownloadInfo[0];
        }
    }
}
