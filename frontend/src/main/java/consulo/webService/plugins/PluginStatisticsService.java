package consulo.webService.plugins;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.PostConstruct;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.intellij.util.containers.ContainerUtil;
import consulo.webService.plugins.mongo.MongoDownloadStat;
import consulo.webService.plugins.mongo.MongoDownloadStatRepository;
import consulo.webService.plugins.mongo.MongoPluginNode;
import consulo.webService.plugins.mongo.MongoPluginNodeRepository;

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

	public void increaseDownload(@NotNull String pluginId, PluginChannel channel, @NotNull String version, @NotNull String platformVersion, boolean viaUpdate)
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

	public int getDownloadStatCount(@NotNull String pluginId, @NotNull PluginChannel pluginChannel)
	{
		DownloadPluginStatistics statistics = myStatistics.get(pluginId);
		if(statistics == null)
		{
			return 0;
		}
		return statistics.byChannel[pluginChannel.ordinal()];
	}

	@NotNull
	public List<MongoDownloadStat> getDownloadStat(@NotNull String pluginId)
	{
		List<MongoDownloadStat> stats = getMongoDownloadStatFromMongo(pluginId);

		PluginInfo pluginInfo = myBlock.myPluginInfos.get(pluginId);
		if(pluginInfo != null)
		{
			return ContainerUtil.concat(stats, pluginInfo.myDownloadStat);
		}
		return stats;
	}

	private List<MongoDownloadStat> getMongoDownloadStatFromMongo(@NotNull String pluginId)
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
