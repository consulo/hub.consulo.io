package org.mustbe.consulo.war.plugins;

import java.io.File;

import org.mustbe.consulo.war.ReleaseManager;

/**
 * @author VISTALL
 * @since 22.04.14
 */
public class PluginManagerNew extends ReleaseManager<PluginDirManager>
{
	public static final PluginManagerNew INSTANCE = new PluginManagerNew();

	public PluginManagerNew()
	{
		super("consulo.plugins.work.dir");
	}

	@Override
	protected PluginDirManager createSnapshotManager(File dir)
	{
		return new SnapshotPluginDirManager(dir);
	}

	@Override
	protected PluginDirManager createStaticManager(File dir)
	{
		return new StaticPluginDirManager(dir);
	}
}
