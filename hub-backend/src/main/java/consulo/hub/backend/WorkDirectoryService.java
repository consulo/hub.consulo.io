package consulo.hub.backend;

import jakarta.annotation.Nonnull;

import java.nio.file.Path;

/**
 * @author VISTALL
 * @since 18/05/2023
 */
public interface WorkDirectoryService
{
	@Nonnull
	Path getWorkingDirectory();
}
