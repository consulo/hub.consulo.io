package org.mustbe.consulo.war;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import javax.validation.constraints.NotNull;

import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.war.util.ApplicationConfiguration;

/**
 * @author VISTALL
 * @since 22.04.14
 */
public abstract class ReleaseManager<T extends ReleaseDirManager>
{
	private static final String SNAPSHOT = "SNAPSHOT";

	protected Map<Integer, T> myData = new ConcurrentSkipListMap<Integer, T>();

	protected File myWorkDir;

	public ReleaseManager(String var)
	{
		String pluginWorkDir = ApplicationConfiguration.getProperty(var);
		myWorkDir = new File(pluginWorkDir);

		new File(myWorkDir, "SNAPSHOT").mkdirs();

		File[] files = myWorkDir.listFiles();
		if(files != null)
		{
			for(File childDir : files)
			{
				int buildNumber = toBuild(childDir.getName());

				T pluginDirManager = null;
				if(buildNumber == Integer.MAX_VALUE)
				{
					pluginDirManager = createSnapshotManager(childDir);
				}
				else
				{
					pluginDirManager = createStaticManager(childDir);
				}
				myData.put(buildNumber, pluginDirManager);
			}
		}
	}

	@NotNull
	public File getWorkDir()
	{
		return myWorkDir;
	}

	public void addBuild(int buildNumber)
	{
		myData.put(buildNumber, createStaticManager(new File(myWorkDir, String.valueOf(buildNumber))));
	}

	@NotNull
	public T findByBuild(int build)
	{
		T pluginDirManager = myData.get(build);
		if(pluginDirManager == null)
		{
			pluginDirManager = myData.get(Integer.MAX_VALUE);
		}
		return pluginDirManager;
	}

	@NotNull
	protected abstract T createSnapshotManager(File dir);

	@NotNull
	protected abstract T createStaticManager(File dir);

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
}
