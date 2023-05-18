package consulo.hub.backend.impl;

import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;
import consulo.hub.backend.TempFileService;
import consulo.hub.backend.WorkDirectoryService;
import consulo.util.lang.StringUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
	private static final Logger LOG = LoggerFactory.getLogger(TempFileServiceImpl.class);

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

	private Path myTempDirPath;

	private WorkDirectoryService myWorkDirectoryService;

	@Autowired
	public TempFileServiceImpl(WorkDirectoryService workDirectoryService)
	{
		myWorkDirectoryService = workDirectoryService;
	}

	@Deprecated
	public TempFileServiceImpl(File tempDirectory)
	{
		this(tempDirectory.toPath());
	}

	public TempFileServiceImpl(Path tempDirectory)
	{
		myTempDirPath = tempDirectory;
	}

	public void setTempDirectory(Path tempDirectory)
	{
		myTempDirPath = tempDirectory;
	}

	@Nonnull
	private Path getTempDirectoryPath()
	{
		return Objects.requireNonNull(myTempDirPath, "not initialized");
	}

	@PostConstruct
	public void init() throws IOException
	{
		WorkDirectoryService workDirectoryService = Objects.requireNonNull(myWorkDirectoryService);

		Path tempDir = workDirectoryService.getWorkingDirectory().resolve("tempDir");
		if(Files.exists(tempDir))
		{
			MoreFiles.deleteRecursively(tempDir.toAbsolutePath(), RecursiveDeleteOption.ALLOW_INSECURE);
		}

		Files.createDirectory(tempDir);

		myTempDirPath = tempDir;
	}

	@Override
	@Nonnull
	public Path createTempDirPath(String prefix) throws IOException
	{
		Path path = getTempDirectoryPath().resolve(prefix);
		if(Files.exists(path))
		{
			MoreFiles.deleteRecursively(path.toAbsolutePath(), RecursiveDeleteOption.ALLOW_INSECURE);
		}

		Files.createDirectory(path);

		return path;
	}

	@Override
	@Nonnull
	public Path createTempFilePath(String prefix, @Nullable String ext) throws IOException
	{
		long l = myTempCount.incrementAndGet();

		Path path = getTempDirectoryPath().resolve(StringUtil.isEmpty(ext) ? prefix + "_" + l : prefix + "_" + l + "." + ext);
		if(Files.exists(path))
		{
			MoreFiles.deleteRecursively(path.toAbsolutePath(), RecursiveDeleteOption.ALLOW_INSECURE);
		}

		return path;
	}

	@Override
	public void asyncDelete(Path... files)
	{
		if(files.length == 0)
		{
			return;
		}

		myExecutor.execute(() ->
		{
			for(Path file : files)
			{
				try
				{
					MoreFiles.deleteRecursively(file.toAbsolutePath(), RecursiveDeleteOption.ALLOW_INSECURE);
				}
				catch(IOException e)
				{
					LOG.warn("Delete problem for path: " + file, e);
				}
			}
		});
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
