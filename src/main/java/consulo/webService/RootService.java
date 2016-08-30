package consulo.webService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.SystemProperties;
import consulo.webService.update.UpdateChannel;
import consulo.webService.update.PluginChannelService;
import consulo.webService.util.ConsuloHelper;

/**
 * @author VISTALL
 * @since 28-Aug-16
 */
@WebListener
public class RootService implements ServletContextListener
{
	private static RootService ourInstance;
	private static boolean ourInitialized;

	@NotNull
	public static RootService getInstance() throws ServiceIsNotReadyException
	{
		if(!ourInitialized)
		{
			throw new ServiceIsNotReadyException();
		}

		return getInstanceNoState();
	}

	@NotNull
	public static RootService getInstanceNoState()
	{
		return ourInstance;
	}

	private final ChildService[] myChildServices;

	private final File myConsuloWebServiceHome;

	private File myTempUploadDirectory;

	private AtomicLong myTempCount = new AtomicLong();

	public RootService()
	{
		List<ChildService> childServiceList = new ArrayList<>();
		for(UpdateChannel channel : UpdateChannel.values())
		{
			childServiceList.add(new PluginChannelService(channel));
		}

		myChildServices = childServiceList.toArray(new ChildService[childServiceList.size()]);

		String userHome = SystemProperties.getUserHome();

		myConsuloWebServiceHome = new File(userHome, ".consuloWebservice");
	}

	public ChildService[] getChildServices()
	{
		return myChildServices;
	}

	@NotNull
	public PluginChannelService getUpdateService(@NotNull UpdateChannel updateChannel)
	{
		for(ChildService childService : myChildServices)
		{
			if(childService instanceof PluginChannelService && ((PluginChannelService) childService).getChannel() == updateChannel)
			{
				return (PluginChannelService) childService;
			}
		}
		throw new IllegalArgumentException(String.valueOf(updateChannel));
	}


	@NotNull
	public File createTempFile(String prefix, String ext)
	{
		long l = myTempCount.incrementAndGet();

		File file = new File(myTempUploadDirectory, prefix + "_" + l + "." + ext);
		if(file.exists())
		{
			FileUtil.delete(file);
		}

		return file;
	}

	@NotNull
	public File getConsuloWebServiceHome()
	{
		return myConsuloWebServiceHome;
	}

	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent)
	{
		ourInstance = this;

		ConsuloHelper.init();

		FileUtil.createDirectory(myConsuloWebServiceHome);

		myTempUploadDirectory = new File(myConsuloWebServiceHome, "tempUpload");

		FileUtil.createDirectory(myTempUploadDirectory);

		File pluginChannelDir = new File(myConsuloWebServiceHome, "plugin");
		FileUtil.createDirectory(pluginChannelDir);

		for(ChildService service : myChildServices)
		{
			service.init(pluginChannelDir);
		}

		ourInitialized = true;
	}

	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent)
	{
		// nothing ?
	}
}
