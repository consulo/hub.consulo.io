package consulo.hub.backend;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * @author VISTALL
 * @since 06/05/2023
 */
public interface TempFileService
{
	@Nonnull
	Path createTempDirPath(String prefix) throws IOException;

	@Nonnull
	Path createTempFilePath(String prefix, @Nullable String ext) throws IOException;

	void asyncDelete(Path... files);

	@Deprecated
	default File createTempDir(String prefix) throws IOException
	{
		return createTempDirPath(prefix).toFile();
	}

	@Deprecated
	default File createTempFile(String prefix, @Nullable String ext) throws IOException
	{
		return createTempFilePath(prefix, ext).toFile();
	}

	@Deprecated
	void asyncDelete(File... files);
}
