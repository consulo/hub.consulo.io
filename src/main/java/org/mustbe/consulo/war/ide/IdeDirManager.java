package org.mustbe.consulo.war.ide;

import java.io.File;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.war.ReleaseDirManager;

/**
 * @author VISTALL
 * @since 22.04.14
 */
public class IdeDirManager implements ReleaseDirManager
{
	protected File myDir;

	public IdeDirManager(File dir)
	{
		myDir = dir;
	}

	@NotNull
	public File getDownloadFile(String os)
	{
		return new File(myDir, "consulo-" + os + ".zip");
	}

	@NotNull
	@Override
	public File getDir()
	{
		return myDir;
	}
}
