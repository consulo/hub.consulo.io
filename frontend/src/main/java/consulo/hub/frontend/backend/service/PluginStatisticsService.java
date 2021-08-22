package consulo.hub.frontend.backend.service;

import consulo.hub.shared.repository.mongo.domain.MongoDownloadStat;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author VISTALL
 * @since 21/08/2021
 */
@Service
public class PluginStatisticsService
{
	public List<MongoDownloadStat> getDownloadStat(String pluginId)
	{
		// TODO not implemented
		return List.of();
	}
}
