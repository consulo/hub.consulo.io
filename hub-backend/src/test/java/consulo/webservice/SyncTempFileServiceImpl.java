package consulo.webservice;

import consulo.hub.backend.WorkDirectoryService;
import consulo.hub.backend.impl.AsyncTempFileServiceImpl;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * @author VISTALL
 * @since 07/07/2023
 */
public class SyncTempFileServiceImpl extends AsyncTempFileServiceImpl
{
	public SyncTempFileServiceImpl(WorkDirectoryService workDirectoryService)
	{
		super(workDirectoryService);
	}

	@Override
	public void asyncDelete(Path... files)
	{
		for(Path file : files)
		{
			try
			{
				FileSystemUtils.deleteRecursively(file);
			}
			catch(IOException e)
			{
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public void asyncDelete(File... files)
	{
		for(File file : files)
		{
			FileSystemUtils.deleteRecursively(file);
		}
	}
}
