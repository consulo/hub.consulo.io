package consulo.webService;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import com.google.common.annotations.VisibleForTesting;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.util.SystemProperties;
import consulo.webService.plugins.PluginChannel;
import consulo.webService.plugins.PluginChannelService;
import consulo.webService.util.ConsuloHelper;

/**
 * @author VISTALL
 * @since 28-Aug-16
 */
@Service
public class PluginChannelsService
{
	private final PluginChannelService[] myChildServices;

	private final File myConsuloWebServiceHome;

	private File myTempUploadDirectory;

	private AtomicLong myTempCount = new AtomicLong();

	private Executor myExecutor = Executors.newFixedThreadPool(Integer.MAX_VALUE);

	public PluginChannelsService()
	{
		this(SystemProperties.getUserHome());
	}

	@VisibleForTesting
	public PluginChannelsService(String userHome)
	{
		ConsuloHelper.init();

		PluginChannel[] values = PluginChannel.values();
		myChildServices = new PluginChannelService[values.length];
		for(int i = 0; i < values.length; i++)
		{
			myChildServices[i] = new PluginChannelService(values[i]);
		}

		myConsuloWebServiceHome = new File(userHome, ".consuloWebservice");

		System.setProperty(PathManager.PROPERTY_HOME_PATH, myConsuloWebServiceHome.getPath());
	}

	@NotNull
	public PluginChannelService getUpdateService(@NotNull PluginChannel channel)
	{
		return myChildServices[channel.ordinal()];
	}

	@NotNull
	public File createTempFile(String prefix, String ext)
	{
		long l = myTempCount.incrementAndGet();

		File file = new File(myTempUploadDirectory, prefix + "_" + l + "." + ext);
		if(file.exists())
		{
			FileUtilRt.delete(file);
		}

		return file;
	}

	public void asyncDelete(File... files)
	{
		if(files.length == 0)
		{
			return;
		}
		myExecutor.execute(() -> {
			for(File file : files)
			{
				FileUtilRt.delete(file);
			}
		});
	}

	@NotNull
	public File getConsuloWebServiceHome()
	{
		return myConsuloWebServiceHome;
	}

	@PostConstruct
	public void contextInitialized()
	{
		FileUtilRt.createDirectory(myConsuloWebServiceHome);

		myTempUploadDirectory = new File(myConsuloWebServiceHome, "tempUpload");
		FileUtilRt.delete(myTempUploadDirectory);
		FileUtilRt.createDirectory(myTempUploadDirectory);

		File pluginChannelDir = new File(myConsuloWebServiceHome, "plugin");
		FileUtilRt.createDirectory(pluginChannelDir);

		for(PluginChannelService service : myChildServices)
		{
			service.initImpl(pluginChannelDir);
		}
	}
}
