package consulo.hub.backend;

import jakarta.annotation.Nullable;

import java.io.File;

/**
 * @author VISTALL
 * @since 06/05/2023
 */
public interface TempFileService
{
	File createTempDir(String prefix);

	File createTempFile(String prefix, @Nullable String ext);

	void asyncDelete(File... files);
}
