package consulo.webService.plugins;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jetbrains.annotations.NotNull;
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
	private static class PluginInfo
	{
		private List<MongoDownloadStat> myDownloadStat = new CopyOnWriteArrayList<>();
	}

	private static class Block
	{
		private final Map<String, PluginInfo> myPluginInfos = new ConcurrentHashMap<>();
	}

	private MongoPluginNodeRepository myMongoPluginNodeRepository;
	private MongoDownloadStatRepository myMongoDownloadStatRepository;

	private Block myBlock = new Block();

	@Autowired
	public PluginStatisticsService(MongoPluginNodeRepository mongoPluginNodeRepository, MongoDownloadStatRepository mongoDownloadStatRepository)
	{
		myMongoPluginNodeRepository = mongoPluginNodeRepository;
		myMongoDownloadStatRepository = mongoDownloadStatRepository;
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
