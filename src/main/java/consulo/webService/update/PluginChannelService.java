package consulo.webService.update;

import java.io.File;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.util.io.FileUtil;
import consulo.webService.ChildService;

/**
 * @author VISTALL
 * @since 28-Aug-16
 */
public class PluginChannelService extends ChildService
{
	private File myPluginChannelDirectory;

	private final UpdateChannel myChannel;

	public PluginChannelService(UpdateChannel channel)
	{
		myChannel = channel;
	}

	public UpdateChannel getChannel()
	{
		return myChannel;
	}

	@NotNull
	public File getPluginChannelDirectory()
	{
		return myPluginChannelDirectory;
	}

	@Override
	public void initImpl(File pluginChannelDir)
	{
		File channelDir = new File(pluginChannelDir, myChannel.name());

		FileUtil.createDirectory(channelDir);

		myPluginChannelDirectory = channelDir;
	}
}
