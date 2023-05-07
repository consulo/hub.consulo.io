package consulo.hub.backend.repository.analyzer;

import com.fasterxml.jackson.databind.ObjectMapper;
import consulo.container.impl.PluginDescriptorImpl;
import consulo.container.plugin.PluginId;
import consulo.hub.backend.TempFileService;
import consulo.hub.backend.repository.PluginAnalyzerService;
import consulo.hub.backend.repository.PluginChannelService;
import consulo.hub.backend.util.ZipUtil;
import consulo.hub.shared.repository.PluginNode;
import consulo.util.collection.ArrayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.*;
import java.util.zip.ZipFile;

/**
 * @author VISTALL
 * @since 20-Sep-16
 */
@Service
@Order(2_000)
public class PluginAnalyzerServiceImpl implements CommandLineRunner, PluginAnalyzerService
{
	private static final Logger LOG = LoggerFactory.getLogger(PluginAnalyzerServiceImpl.class);

	private final TempFileService myTempFileService;

	private final PluginAnalyzerEnv myPluginAnalyzerEnv;

	private final ObjectMapper myObjectMapper;

	@Autowired
	public PluginAnalyzerServiceImpl(TempFileService tempFileService, ObjectMapper objectMapper)
	{
		myTempFileService = tempFileService;
		myObjectMapper = objectMapper;
		myPluginAnalyzerEnv = new PluginAnalyzerEnv(tempFileService);
	}

	@Override
	public void run(String[] args) throws Exception
	{
		myPluginAnalyzerEnv.init();
	}

	@Override
	@Nonnull
	public PluginNode.ExtensionPreview[] analyze(File deployHome, PluginDescriptorImpl descriptor, PluginChannelService channelService) throws Exception
	{
		File[] forRemove = new File[0];

		List<File> pluginDirs = new ArrayList<>();

		Set<String> dependencies = new HashSet<>();
		collectAllDependencies(descriptor.getPluginId().getIdString(), Arrays.stream(descriptor.getDependentPluginIds()).map(PluginId::getIdString).toArray(String[]::new), channelService,
				dependencies, new HashSet<>());

		if(!dependencies.isEmpty())
		{
			File analyzeUnzip = myTempFileService.createTempFile("plugin_deps_" + descriptor.getPluginId(), "");
			forRemove = ArrayUtil.append(forRemove, analyzeUnzip);
			pluginDirs.add(analyzeUnzip);

			for(String dependencyId : dependencies)
			{
				PluginNode pluginNode = channelService.select(PluginChannelService.SNAPSHOT, dependencyId, null, false);
				if(pluginNode == null)
				{
					continue;
				}


				try (ZipFile zipFile = new ZipFile(pluginNode.targetFile))
				{
					ZipUtil.extract(zipFile, analyzeUnzip);
				}
			}
		}

		try
		{
			PluginAnalyzerRunner runner = new PluginAnalyzerRunner(myPluginAnalyzerEnv, myObjectMapper);

			pluginDirs.add(deployHome);

			return runner.run(descriptor.getPluginId().getIdString(), pluginDirs.stream().map(File::getPath).toArray(String[]::new));
		}
		finally
		{
			myTempFileService.asyncDelete(forRemove);
		}
	}

	private void collectAllDependencies(String pluginId, String[] dependencies, PluginChannelService channelService, Set<String> processed, Set<String> result)
	{
		if(!processed.add(pluginId))
		{
			return;
		}

		for(String dependencyId : dependencies)
		{
			PluginNode dependPlugin = channelService.select(PluginChannelService.SNAPSHOT, dependencyId, null, false);
			if(dependPlugin == null)
			{
				continue;
			}

			result.add(dependencyId);

			collectAllDependencies(dependPlugin.id, dependPlugin.dependencies, channelService, processed, result);
		}
	}
}
