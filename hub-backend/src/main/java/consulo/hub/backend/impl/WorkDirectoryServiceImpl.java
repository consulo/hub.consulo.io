package consulo.hub.backend.impl;

import consulo.hub.backend.WorkDirectoryService;
import jakarta.annotation.Nonnull;
import jakarta.annotation.PostConstruct;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * @author VISTALL
 * @since 18/05/2023
 */
public class WorkDirectoryServiceImpl implements WorkDirectoryService
{
	private final String myWorkingDirectoryPath;

	private Path myWorkPath;

	public WorkDirectoryServiceImpl(String workingDirectoryPath)
	{
		myWorkingDirectoryPath = workingDirectoryPath;
	}

	@PostConstruct
	public void init() throws Exception
	{
		myWorkPath = Path.of(myWorkingDirectoryPath);
		if(!Files.exists(myWorkPath))
		{
			Files.createDirectory(myWorkPath);
		}
	}

	@Nonnull
	@Override
	public Path getWorkingDirectory()
	{
		return Objects.requireNonNull(myWorkPath, "not initialized");
	}
}
