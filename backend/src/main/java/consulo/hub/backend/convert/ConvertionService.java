package consulo.hub.backend.convert;

import consulo.hub.backend.property.PropertyService;
import consulo.hub.backend.repository.mongo.MongoPluginNodeRepository;
import consulo.hub.backend.repository.repository.RepositoryDownloadInfoRepository;
import consulo.hub.shared.repository.domain.RepositoryDownloadInfo;
import consulo.hub.shared.repository.mongo.domain.MongoDownloadStat;
import consulo.hub.shared.repository.mongo.domain.MongoPluginNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @since 28/08/2021
 */
@Service
public class ConvertionService
{
	private static final Logger LOG = LoggerFactory.getLogger(ConvertionService.class);

	@Autowired
	private PropertyService myPropertyService;

	@Autowired
	private RepositoryDownloadInfoRepository myRepositoryDownloadInfoRepository;

	@Autowired
	private MongoPluginNodeRepository myMongoPluginNodeRepository;

	@PostConstruct
	public void start()
	{
		if(!myPropertyService.getValue("download_conversion"))
		{
			convertDownloads();
			myPropertyService.setValue("download_conversion", true);
		}
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
