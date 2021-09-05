package consulo.hub.backend.convert;

import consulo.hub.backend.auth.repository.UserAccountRepository;
import consulo.hub.backend.property.PropertyService;
import consulo.hub.backend.repository.mongo.MongoPluginNodeRepository;
import consulo.hub.backend.repository.repository.RepositoryDownloadInfoRepository;
import consulo.hub.backend.statistics.mongo.MongoStatisticRepository;
import consulo.hub.backend.statistics.repository.StatisticEntryRepository;
import consulo.hub.shared.auth.domain.UserAccount;
import consulo.hub.shared.repository.domain.RepositoryDownloadInfo;
import consulo.hub.shared.repository.mongo.domain.MongoDownloadStat;
import consulo.hub.shared.repository.mongo.domain.MongoPluginNode;
import consulo.hub.shared.statistics.domain.MongoStatisticBean;
import consulo.hub.shared.statistics.domain.StatisticEntry;
import consulo.hub.shared.statistics.domain.StatisticUsageGroup;
import consulo.hub.shared.statistics.domain.StatisticUsageGroupValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author VISTALL
 * @since 28/08/2021
 */
//@Service
public class ConvertionService
{
	private static final Logger LOG = LoggerFactory.getLogger(ConvertionService.class);

	@Autowired
	private UserAccountRepository myUserAccountRepository;

	@Autowired
	private PropertyService myPropertyService;

	@Autowired
	private RepositoryDownloadInfoRepository myRepositoryDownloadInfoRepository;

	@Autowired
	@Lazy
	private MongoPluginNodeRepository myMongoPluginNodeRepository;

	@Autowired
	private StatisticEntryRepository myStatisticEntryRepository;

	@Autowired
	private MongoStatisticRepository myMongoStatisticRepository;

	@PostConstruct
	public void start()
	{
		if(!myPropertyService.getValue("download_conversion"))
		{
			convertDownloads();
			myPropertyService.setValue("download_conversion", true);
		}

		if(!myPropertyService.getValue("statistics_conversion"))
		{
			convertStatistics();
			myPropertyService.setValue("statistics_conversion", true);
		}
	}

	private void convertStatistics()
	{
		LOG.info("Starting conversion statistics");

		myStatisticEntryRepository.deleteAll();

		List<MongoStatisticBean> all = myMongoStatisticRepository.findAll();

		List<StatisticEntry> statisticEntries = new ArrayList<>(all.size());

		Map<String, UserAccount> users = new HashMap<>();

		for(MongoStatisticBean bean : all)
		{
			StatisticEntry s = new StatisticEntry();
			statisticEntries.add(s);

			s.setKey(bean.key);
			s.setCreateTime(bean.getCreateTime());
			s.setInstallationID(bean.getInstallationID());

			UserAccount userAccount = users.computeIfAbsent(bean.getOwnerEmail(), myUserAccountRepository::findByUsername);
			s.setUser(userAccount);

			for(MongoStatisticBean.UsageGroup mongoGroup : bean.getGroups())
			{
				StatisticUsageGroup usageGroup = new StatisticUsageGroup();
				usageGroup.setUsageGroupId(mongoGroup.getUsageGroupId());

				for(MongoStatisticBean.UsageGroupValue mongoGroupValue : mongoGroup.getValues())
				{
					StatisticUsageGroupValue usageGroupValue = new StatisticUsageGroupValue();
					usageGroupValue.setUsageGroupValueId(mongoGroupValue.getUsageGroupValueId());
					usageGroupValue.setCount(mongoGroupValue.getCount());

					usageGroup.getValues().add(usageGroupValue);
				}

				s.getGroups().add(usageGroup);
			}
		}

		myStatisticEntryRepository.save(statisticEntries);

		LOG.info("Finished conversion statistics. Count: " + myStatisticEntryRepository.count());
	}

	private void convertDownloads()
	{
		LOG.info("Starting conversion downloads");

		myRepositoryDownloadInfoRepository.deleteAll();

		List<MongoPluginNode> nodes = myMongoPluginNodeRepository.findAll();

		for(MongoPluginNode node : nodes)
		{
			List<RepositoryDownloadInfo> downloadInfos = new ArrayList<>(node.getDownloadStat().size());

			for(MongoDownloadStat downloadStat : node.getDownloadStat())
			{
				downloadInfos.add(new RepositoryDownloadInfo(downloadStat.getTime(), node.getId(), downloadStat.getChannel(), downloadStat.getVersion(), downloadStat.getPlatformVersion(),
						downloadStat.getViaUpdate() == Boolean.TRUE));
			}

			myRepositoryDownloadInfoRepository.save(downloadInfos);
		}

		LOG.info("Finished conversion downloads. Count: " + myRepositoryDownloadInfoRepository.count());
	}
}
