package org.mustbe.consulo.war;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.war.util.ApplicationConfiguration;

/**
 * @author VISTALL
 * @since 22.04.14
 */
public class PluginManagerNew
{
	public static final PluginManagerNew INSTANCE = new PluginManagerNew();
	private static final String SNAPSHOT = "SNAPSHOT";

	private Map<Integer, PluginDirManager> myPluginDirs = new ConcurrentSkipListMap<Integer, PluginDirManager>();

	private File myWorkDir;

	public PluginManagerNew()
	{
		String pluginWorkDir = ApplicationConfiguration.getProperty("consulo.plugins.work.dir");
		myWorkDir = new File(pluginWorkDir);

		new File(myWorkDir, "SNAPSHOT").mkdirs();

		File[] files = myWorkDir.listFiles();
		if(files != null)
		{
			for(File childDir : files)
			{
				int buildNumber = toBuild(childDir.getName());

				PluginDirManager pluginDirManager = null;
				if(buildNumber == Integer.MAX_VALUE)
				{
					pluginDirManager = new SnapshotPluginDirManager(childDir);
				}
				else
				{
					pluginDirManager = new StaticPluginDirManager(childDir);
				}
				myPluginDirs.put(buildNumber, pluginDirManager);
			}
		}
	}

	@NotNull
	public PluginDirManager findPluginDir(int build)
	{
		PluginDirManager pluginDirManager = myPluginDirs.get(build);
		if(pluginDirManager == null)
		{
			pluginDirManager = myPluginDirs.get(Integer.MAX_VALUE);
		}
		return pluginDirManager;
	}

	public static int toBuild(@Nullable String str)
	{
		if(SNAPSHOT.equals(str) || str == null)
		{
			return Integer.MAX_VALUE;
		}
		try
		{
			return Integer.parseInt(str);
		}
		catch(NumberFormatException e)
		{
			return Integer.MAX_VALUE;
		}
	}

	@NotNull
	public File getWorkDir()
	{
		return myWorkDir;
	}

	public void addPluginBuild(int buildNumber)
	{
		myPluginDirs.put(buildNumber, new StaticPluginDirManager(new File(myWorkDir, String.valueOf(buildNumber))));
	}
}
