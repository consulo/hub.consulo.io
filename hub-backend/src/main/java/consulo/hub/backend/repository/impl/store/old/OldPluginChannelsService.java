package consulo.hub.backend.repository.impl.store.old;

import consulo.hub.backend.impl.TempFileServiceImpl;
import consulo.hub.backend.repository.RepositoryChannelsService;
import consulo.hub.backend.WorkDirectoryService;
import consulo.hub.backend.repository.analyzer.PluginAnalyzerServiceImpl;
import consulo.hub.shared.repository.PluginChannel;
import consulo.util.io.FileUtil;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskExecutor;
import org.springframework.util.FileSystemUtils;

import java.io.File;

/**
 * @author VISTALL
 * @since 28-Aug-16
 */
@Deprecated
public class OldPluginChannelsService implements RepositoryChannelsService
{
	private static final Logger LOG = LoggerFactory.getLogger(PluginAnalyzerServiceImpl.class);

	private final OldPluginChannelService[] myPluginChannelServices;
	private final WorkDirectoryService myWorkDirectoryService;
	private final TempFileServiceImpl myFileService;
	private final TaskExecutor myTaskExecutor;

	public OldPluginChannelsService(WorkDirectoryService workDirectoryService, TempFileServiceImpl fileService, TaskExecutor taskExecutor)
	{
		myWorkDirectoryService = workDirectoryService;
		myFileService = fileService;
		myTaskExecutor = taskExecutor;

		PluginChannel[] values = PluginChannel.values();
		myPluginChannelServices = new OldPluginChannelService[values.length];
		for(int i = 0; i < values.length; i++)
		{
			myPluginChannelServices[i] = new OldPluginChannelService(values[i]);
		}
	}

	@Nonnull
	@Override
	public String getDeployPluginExtension()
	{
		return "zip";
	}

	@Override
	@Nonnull
	public OldPluginChannelService getRepositoryByChannel(@Nonnull PluginChannel channel)
	{
		return myPluginChannelServices[channel.ordinal()];
	}

	public void init()
	{
		initWorkingDirectory(myWorkDirectoryService.getWorkingDirectory().toAbsolutePath().toString());
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

		myFileService.setTempDirectory(tempUpload.toPath());

		File pluginChannelDir = new File(workingDirectory, "plugin");
		FileUtil.createDirectory(pluginChannelDir);

		myTaskExecutor.execute(() ->
		{
			for(OldPluginChannelService service : myPluginChannelServices)
			{
				try
				{
					service.initImpl(pluginChannelDir);
				}
				catch(Exception e)
				{
					LOG.error(e.getMessage(), e);
				}
			}
		});
	}
}
