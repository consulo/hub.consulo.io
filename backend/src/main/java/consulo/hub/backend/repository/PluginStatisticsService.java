package consulo.hub.backend.repository;

import consulo.hub.backend.repository.repository.RepositoryDownloadInfoRepository;
import consulo.hub.shared.repository.PluginChannel;
import consulo.hub.shared.repository.domain.RepositoryDownloadInfo;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author VISTALL
 * @since 04-Jan-17
 */
@Service
public class PluginStatisticsService
{
	private static final Logger logger = LoggerFactory.getLogger(PluginStatisticsService.class);

	private static class Block
	{
		private final List<RepositoryDownloadInfo> myDownloadStat = new CopyOnWriteArrayList<>();
	}

	private static class DownloadPluginStatistics
	{
		private int[] byChannel = new int[PluginChannel.values().length];
	}

	private final RepositoryDownloadInfoRepository myRepositoryDownloadInfoRepository;

	private volatile Block myBlock = new Block();

	private final Map<String, DownloadPluginStatistics> myStatistics = new ConcurrentHashMap<>();

	@Autowired
	public PluginStatisticsService(RepositoryDownloadInfoRepository repositoryDownloadInfoRepository)
	{
		myRepositoryDownloadInfoRepository = repositoryDownloadInfoRepository;
	}

	@Scheduled(cron = "0 0 * * * *")
	@PostConstruct
	private void updateDownloadStatistics()
	{
		Map<String, DownloadPluginStatistics> map = new HashMap<>();

		List<RepositoryDownloadInfo> all = myRepositoryDownloadInfoRepository.findAll();
		for(RepositoryDownloadInfo stat : all)
		{
			DownloadPluginStatistics statistics = map.computeIfAbsent(stat.getPluginId(), id -> new DownloadPluginStatistics());

			try
			{
				String channel = stat.getChannel();
				if (channel.equals("valhalla"))
				{
					continue;
				}

				PluginChannel pluginChannel = PluginChannel.valueOf(channel);

				statistics.byChannel[pluginChannel.ordinal()]++;
			}
			catch(IllegalArgumentException e)
			{
				logger.error(e.getMessage(), e);
			}
		}

		myStatistics.putAll(map);
	}

	public void increaseDownload(@Nonnull String pluginId, PluginChannel channel, @Nonnull String version, @Nonnull String platformVersion, boolean viaUpdate)
	{
		Block block = myBlock;

		RepositoryDownloadInfo downloadStat = new RepositoryDownloadInfo(System.currentTimeMillis(), pluginId, channel.name(), version, platformVersion, viaUpdate);

		block.myDownloadStat.add(downloadStat);
	}

	public int getDownloadStatCount(@Nonnull String pluginId, @Nonnull PluginChannel pluginChannel)
	{
		DownloadPluginStatistics statistics = myStatistics.get(pluginId);
		if(statistics == null)
		{
			return 0;
		}
		return statistics.byChannel[pluginChannel.ordinal()];
	}

	public int getDownloadStatCountAll(@Nonnull String pluginId)
	{
		DownloadPluginStatistics statistics = myStatistics.get(pluginId);
		if(statistics == null)
		{
			return 0;
		}
		int count = 0;

		for(PluginChannel channel : PluginChannel.values())
		{
			count += statistics.byChannel[channel.ordinal()];
		}
		return count;
	}

	@Nonnull
	public List<RepositoryDownloadInfo> getDownloadStat(@Nonnull String pluginId)
	{
		List<RepositoryDownloadInfo> stats = new ArrayList<>(getMongoDownloadStatFromMongo(pluginId));

		for(RepositoryDownloadInfo info : myBlock.myDownloadStat)
		{
			if(Objects.equals(info.getPluginId(), pluginId))
			{
				stats.add(info);
			}
		}
		return stats;
	}

	private List<RepositoryDownloadInfo> getMongoDownloadStatFromMongo(@Nonnull String pluginId)
	{
		return myRepositoryDownloadInfoRepository.findAllByPluginId(pluginId);
	}

	@Scheduled(fixedRate = 60 * 1000)
	private void tick()
	{
		Block block = myBlock;

		myBlock = new Block();

		myRepositoryDownloadInfoRepository.saveAll(block.myDownloadStat);
	}
}
