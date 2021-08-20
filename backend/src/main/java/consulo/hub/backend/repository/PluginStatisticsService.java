package consulo.hub.backend.repository;

import com.intellij.util.containers.ContainerUtil;
import consulo.hub.backend.repository.mongo.MongoDownloadStatRepository;
import consulo.hub.backend.repository.mongo.MongoPluginNodeRepository;
import consulo.hub.shared.repository.PluginChannel;
import consulo.hub.shared.repository.mongo.domain.MongoDownloadStat;
import consulo.hub.shared.repository.mongo.domain.MongoPluginNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
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

	private static class PluginInfo
	{
		private List<MongoDownloadStat> myDownloadStat = new CopyOnWriteArrayList<>();
	}

	private static class Block
	{
		private final Map<String, PluginInfo> myPluginInfos = new ConcurrentHashMap<>();
	}

	private static class DownloadPluginStatistics
	{
		private int[] byChannel = new int[PluginChannel.values().length];
	}

	private MongoPluginNodeRepository myMongoPluginNodeRepository;
	private MongoDownloadStatRepository myMongoDownloadStatRepository;

	private Block myBlock = new Block();

	private final Map<String, DownloadPluginStatistics> myStatistics = new ConcurrentHashMap<>();

	@Autowired
	public PluginStatisticsService(MongoPluginNodeRepository mongoPluginNodeRepository, MongoDownloadStatRepository mongoDownloadStatRepository)
	{
		myMongoPluginNodeRepository = mongoPluginNodeRepository;
		myMongoDownloadStatRepository = mongoDownloadStatRepository;
	}

	@Scheduled(cron = "0 0 * * * *")
	@PostConstruct
	private void updateDownloadStatistics()
	{
		Map<String, DownloadPluginStatistics> map = new HashMap<>();

		List<MongoPluginNode> all = myMongoPluginNodeRepository.findAll();
		for(MongoPluginNode pluginNode : all)
		{
			DownloadPluginStatistics statistics = map.computeIfAbsent(pluginNode.getId(), id -> new DownloadPluginStatistics());

			List<MongoDownloadStat> downloadStat = pluginNode.getDownloadStat();
			PluginInfo pluginInfo = myBlock.myPluginInfos.get(pluginNode.getId());
			if(pluginInfo != null)
			{
				downloadStat = ContainerUtil.concat(downloadStat, pluginInfo.myDownloadStat);
			}

			for(MongoDownloadStat mongoDownloadStat : downloadStat)
			{
				try
				{
					PluginChannel pluginChannel = PluginChannel.valueOf(mongoDownloadStat.getChannel());

					statistics.byChannel[pluginChannel.ordinal()] ++;
				}
				catch(IllegalArgumentException e)
				{
					logger.error(e.getMessage(), e);
				}
			}
		}

		myStatistics.putAll(map);
	}

	public void increaseDownload(@Nonnull String pluginId, PluginChannel channel, @Nonnull String version, @Nonnull String platformVersion, boolean viaUpdate)
	{
		Block block = myBlock;

		PluginInfo info = block.myPluginInfos.computeIfAbsent(pluginId, s -> new PluginInfo());

		MongoDownloadStat downloadStat = new MongoDownloadStat(System.currentTimeMillis(), channel, version, platformVersion);
		if(viaUpdate)
		{
			downloadStat.setViaUpdate(true);
		}
		info.myDownloadStat.add(downloadStat);
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

	@Nonnull
	public List<MongoDownloadStat> getDownloadStat(@Nonnull String pluginId)
	{
		List<MongoDownloadStat> stats = getMongoDownloadStatFromMongo(pluginId);

		PluginInfo pluginInfo = myBlock.myPluginInfos.get(pluginId);
		if(pluginInfo != null)
		{
			return ContainerUtil.concat(stats, pluginInfo.myDownloadStat);
		}
		return stats;
	}

	private List<MongoDownloadStat> getMongoDownloadStatFromMongo(@Nonnull String pluginId)
	{
		MongoPluginNode pluginNode = myMongoPluginNodeRepository.findOne(pluginId);
		if(pluginNode == null)
		{
			return Collections.emptyList();
		}
		return pluginNode.getDownloadStat();
	}

	@Scheduled(fixedRate = 60 * 1000)
	private void tick()
	{
		Block block = myBlock;
		myBlock = new Block();

		Map<String, PluginInfo> pluginInfos = block.myPluginInfos;
		if(pluginInfos.isEmpty())
		{
			return;
		}

		List<MongoPluginNode> nodes = new ArrayList<>(pluginInfos.size());

		for(Map.Entry<String, PluginInfo> entry : pluginInfos.entrySet())
		{
			MongoPluginNode pluginNode = myMongoPluginNodeRepository.findOne(entry.getKey());
			if(pluginNode == null)
			{
				pluginNode = new MongoPluginNode(entry.getKey());
			}

			List<MongoDownloadStat> downloads = entry.getValue().myDownloadStat;

			myMongoDownloadStatRepository.save(downloads);

			pluginNode.getDownloadStat().addAll(downloads);

			nodes.add(pluginNode);
		}

		myMongoPluginNodeRepository.save(nodes);
	}
}
