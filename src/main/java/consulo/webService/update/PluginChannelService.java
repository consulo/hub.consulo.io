package consulo.webService.update;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.util.ThrowableConsumer;
import consulo.webService.ChildService;

/**
 * @author VISTALL
 * @since 28-Aug-16
 */
public class PluginChannelService extends ChildService
{
	private static class PluginsState
	{
		private Map<String, PluginNode[]> myPlugins = new ConcurrentHashMap<>();

		private File myPluginDirectory;

		private String myPluginId;

		private PluginsState(File rootDir, String pluginId)
		{
			myPluginId = pluginId;
			myPluginDirectory = new File(rootDir, pluginId);

			FileUtilRt.createParentDirs(myPluginDirectory);
		}

		@NotNull
		public File getFileForPlugin(String version)
		{
			File zipFile = new File(myPluginDirectory, myPluginId + "_" + version + ".zip");
			FileUtilRt.createParentDirs(zipFile);
			if(zipFile.exists())
			{
				throw new IllegalArgumentException("Plugin " + myPluginId + "=" + version + " is already uploaded");
			}
			return zipFile;
		}
	}

	private File myPluginChannelDirectory;

	private final UpdateChannel myChannel;

	private ConcurrentMap<String, PluginsState> myPlugins = new ConcurrentHashMap<>();

	public PluginChannelService(UpdateChannel channel)
	{
		myChannel = channel;
	}

	public void push(PluginNode pluginNode, ThrowableConsumer<File, IOException> writeConsumer) throws IOException
	{
		PluginsState pluginsState = myPlugins.computeIfAbsent(pluginNode.id, id -> new PluginsState(myPluginChannelDirectory, pluginNode.id));

		File fileForPlugin = pluginsState.getFileForPlugin(pluginNode.version);

		writeConsumer.consume(fileForPlugin);
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
