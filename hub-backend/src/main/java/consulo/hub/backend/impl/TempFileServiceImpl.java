package consulo.hub.backend.impl;

import consulo.hub.backend.TempFileService;
import consulo.util.lang.StringUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author VISTALL
 * @since 06/05/2023
 */
@Service
public class TempFileServiceImpl implements TempFileService
{
	private AtomicLong myTempCount = new AtomicLong();

	private Executor myExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), new ThreadFactory()
	{
		private final ThreadGroup myGroup = new ThreadGroup("async delete");

		{
			myGroup.setMaxPriority(Thread.MIN_PRIORITY);
		}

		@Nonnull
		@Override
		public Thread newThread(@Nonnull Runnable r)
		{
			return new Thread(myGroup, r);
		}
	});

	private File myTempDirectory;

	@Autowired
	public TempFileServiceImpl()
	{
	}

	public TempFileServiceImpl(File tempDirectory)
	{
		myTempDirectory = tempDirectory;
	}

	public void setTempDirectory(File tempDirectory)
	{
		myTempDirectory = tempDirectory;
	}

	private File getTempDirectory()
	{
		return Objects.requireNonNull(myTempDirectory, "not initialized");
	}

	@Override
	@Nonnull
	public File createTempDir(String prefix)
	{
		File file = new File(getTempDirectory(), prefix);
		if(file.exists())
		{
			FileSystemUtils.deleteRecursively(file);
		}

		file.mkdirs();

		return file;
	}

	@Override
	@Nonnull
	public File createTempFile(String prefix, @Nullable String ext)
	{
		long l = myTempCount.incrementAndGet();

		File file = new File(getTempDirectory(), StringUtil.isEmpty(ext) ? prefix + "_" + l : prefix + "_" + l + "." + ext);
		if(file.exists())
		{
			FileSystemUtils.deleteRecursively(file);
		}

		return file;
	}

	@Override
	public void asyncDelete(File... files)
	{
		if(files.length == 0)
		{
			return;
		}
		myExecutor.execute(() ->
		{
			for(File file : files)
			{
				FileSystemUtils.deleteRecursively(file);
			}
		});
	}
}
