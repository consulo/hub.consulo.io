package consulo.hub.backend.repository;

import consulo.hub.backend.impl.TempFileServiceImpl;
import consulo.hub.backend.repository.analyzer.PluginAnalyzerServiceImpl;
import consulo.hub.backend.util.ConsuloHelper;
import consulo.hub.shared.repository.PluginChannel;
import consulo.util.io.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import javax.annotation.Nonnull;
import java.io.File;

/**
 * @author VISTALL
 * @since 28-Aug-16
 */
@Service
@Order(1_000)
public class PluginChannelsService implements CommandLineRunner
{
	private static final Logger LOGGER = LoggerFactory.getLogger(PluginAnalyzerServiceImpl.class);

	private final PluginChannelService[] myPluginChannelServices;
	private final String myWorkingDirectoryPath;
	private final TempFileServiceImpl myFileService;
	private final TaskExecutor myTaskExecutor;

	@Autowired
	public PluginChannelsService(@Value("${working.directory:hub-workdir}") String workingDirectoryPath, TempFileServiceImpl fileService, TaskExecutor taskExecutor)
	{
		myWorkingDirectoryPath = workingDirectoryPath;
		myFileService = fileService;
		myTaskExecutor = taskExecutor;

		ConsuloHelper.init();

		PluginChannel[] values = PluginChannel.values();
		myPluginChannelServices = new PluginChannelService[values.length];
		for(int i = 0; i < values.length; i++)
		{
			myPluginChannelServices[i] = new PluginChannelService(values[i]);
		}
	}

	@Nonnull
	public PluginChannelService getRepositoryByChannel(@Nonnull PluginChannel channel)
	{
		return myPluginChannelServices[channel.ordinal()];
	}

	@Override
	public void run(String... args) throws Exception
	{
		initWorkingDirectory(myWorkingDirectoryPath);
	}

	private void initWorkingDirectory(String workDirValue)
	{
		if(workDirValue == null)
		{
			throw new Error("Working directory must be not null");
		}

		File workingDirectory = new File(workDirValue);
		FileUtil.createDirectory(workingDirectory);

		File tempUpload = new File(workingDirectory, "temp");
		FileSystemUtils.deleteRecursively(tempUpload);
		FileUtil.createDirectory(tempUpload);

		myFileService.setTempDirectory(tempUpload);

		File pluginChannelDir = new File(workingDirectory, "plugin");
		FileUtil.createDirectory(pluginChannelDir);

		myTaskExecutor.execute(() ->
		{
			for(PluginChannelService service : myPluginChannelServices)
			{
				try
				{
					service.initImpl(pluginChannelDir);
				}
				catch(Exception e)
				{
					LOGGER.error(e.getMessage(), e);
				}
			}
		});
	}
}
