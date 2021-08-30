package consulo.hub.frontend.backend.service;

import consulo.hub.frontend.backend.BackendRequestor;
import consulo.hub.shared.repository.domain.RepositoryDownloadInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author VISTALL
 * @since 21/08/2021
 */
@Service
public class BackendPluginStatisticsService
{
	private static final Logger LOG = LoggerFactory.getLogger(BackendPluginStatisticsService.class);

	@Autowired
	private BackendRequestor myBackendRequestor;

	public RepositoryDownloadInfo[] getDownloadStat(String pluginId)
	{
		try
		{
			return myBackendRequestor.runRequest("/repository/downloadStat", Map.of("pluginId", pluginId), RepositoryDownloadInfo[].class);
		}
		catch(Exception e)
		{
			LOG.warn("Fail to get download stat: " + pluginId, e);
			return new RepositoryDownloadInfo[0];
		}
	}
}
