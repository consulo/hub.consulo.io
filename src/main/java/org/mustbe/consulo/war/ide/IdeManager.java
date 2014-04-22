package org.mustbe.consulo.war.ide;

import java.io.File;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.war.ReleaseManager;
import org.mustbe.consulo.war.util.ApplicationConfiguration;

/**
 * @author VISTALL
 * @since 22.04.14
 */
public class IdeManager extends ReleaseManager<IdeDirManager>
{
	public static final IdeManager INSTANCE = new IdeManager();

	public IdeManager()
	{
		super("consulo.release.work.dir");
	}

	@Override
	protected IdeDirManager createSnapshotManager(File dir)
	{
		return new IdeDirManager(dir)
		{
			@NotNull
			@Override
			public File getDownloadFile(String os)
			{
				String vulcanWorkDir = ApplicationConfiguration.getProperty("vulcan.dir");
				if(vulcanWorkDir == null)
				{
					return super.getDownloadFile(os);
				}

				return new File(vulcanWorkDir, "work/consulo/out/artifacts/dist/consulo-" + os + ".zip");
			}
		};
	}

	@Override
	protected IdeDirManager createStaticManager(File dir)
	{
		return new IdeDirManager(dir);
	}
}
