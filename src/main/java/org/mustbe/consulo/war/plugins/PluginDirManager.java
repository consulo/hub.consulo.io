package org.mustbe.consulo.war.plugins;

import java.io.File;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.war.ReleaseDirManager;

/**
 * @author VISTALL
 * @since 22.04.14
 */
public abstract class PluginDirManager implements ReleaseDirManager
{
	protected static final String EMPTY = "<plugin-repository></plugin-repository>";

	protected final File myDir;

	protected PluginDirManager(File dir)
	{
		myDir = dir;
	}

	@NotNull
	public File getDir()
	{
		return myDir;
	}

	@NotNull
	public abstract String getXmlListText();

	@NotNull
	public File getPlugin(String id)
	{
		return new File(myDir, id + ".zip");
	}
}
