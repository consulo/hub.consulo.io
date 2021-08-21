package consulo.hub.backend.repository;

import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.util.text.StringUtil;
import consulo.hub.backend.util.ConsuloHelper;
import consulo.hub.shared.repository.PluginChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author VISTALL
 * @since 28-Aug-16
 */
@Service
public class PluginChannelsService implements CommandLineRunner
{
	private static final Logger LOGGER = LoggerFactory.getLogger(PluginAnalyzerService.class);

	private final PluginChannelService[] myPluginChannelServices;
	private final String myWorkingDirectoryPath;

	private File myTempUploadDirectory;

	private AtomicLong myTempCount = new AtomicLong();

	private Executor myExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), new ThreadFactory()
	{
		private final ThreadGroup myGroup = new ThreadGroup("async delete");

		{
			myGroup.setMaxPriority(Thread.MIN_PRIORITY);
		}

		@Nonnull
		@Override
		public Thread newThread(@Nonnull Runnable r)
		{
			return new Thread(myGroup, r);
		}
	});

	@Autowired
	public PluginChannelsService(@Value("${working.directory}") String workingDirectoryPath)
	{
		myWorkingDirectoryPath = workingDirectoryPath;
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

	@Nonnull
	public File createTempFile(String prefix, @Nullable String ext)
	{
		long l = myTempCount.incrementAndGet();

		File file = new File(myTempUploadDirectory, StringUtil.isEmpty(ext) ? prefix + "_" + l : prefix + "_" + l + "." + ext);
		if(file.exists())
		{
			FileSystemUtils.deleteRecursively(file);
		}

		return file;
	}

	public void asyncDelete(File... files)
	{
		if(files.length == 0)
		{
			return;
		}
		myExecutor.execute(() ->
		{
			for(File file : files)
			{
				FileSystemUtils.deleteRecursively(file);
			}
		});
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
		FileUtilRt.createDirectory(workingDirectory);

		myTempUploadDirectory = new File(workingDirectory, "tempUpload");
		FileSystemUtils.deleteRecursively(myTempUploadDirectory);
		FileUtilRt.createDirectory(myTempUploadDirectory);

		File pluginChannelDir = new File(workingDirectory, "plugin");
		FileUtilRt.createDirectory(pluginChannelDir);

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
	}
}
