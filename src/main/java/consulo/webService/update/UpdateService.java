package consulo.webService.update;

import java.io.File;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.ServletContextEvent;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.util.io.FileUtil;
import consulo.webService.ChildService;
import consulo.webService.RootController;

/**
 * @author VISTALL
 * @since 28-Aug-16
 */
public class UpdateService extends ChildService
{
	private File[] myPluginChannelDirectories;
	private File myTempUploadDirectory;
	private AtomicLong myTempCount = new AtomicLong();

	@NotNull
	public File getPluginChannelDirectory(UpdateChannel updateChannel)
	{
		assert myPluginChannelDirectories != null;
		return myPluginChannelDirectories[updateChannel.ordinal()];
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

	@Override
	public void contextInitializedImpl(ServletContextEvent servletContextEvent)
	{
		File consuloWebServiceHome = RootController.getInstanceNoState().getConsuloWebServiceHome();

		File pluginChannelDir = new File(consuloWebServiceHome, "plugin");
		FileUtil.createDirectory(pluginChannelDir);

		myTempUploadDirectory = new File(consuloWebServiceHome, "tempUpload");
		FileUtil.createDirectory(myTempUploadDirectory);

		UpdateChannel[] values = UpdateChannel.values();
		myPluginChannelDirectories = new File[values.length];
		for(int i = 0; i < myPluginChannelDirectories.length; i++)
		{
			File channelDir = new File(pluginChannelDir, values[i].name());
			FileUtil.createDirectory(channelDir);

			myPluginChannelDirectories[i] = channelDir;
		}
	}
}
