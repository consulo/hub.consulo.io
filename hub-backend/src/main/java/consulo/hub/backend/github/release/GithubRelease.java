package consulo.hub.backend.github.release;

import jakarta.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author VISTALL
 * @since 18/05/2023
 */
public interface GithubRelease
{
	@Nullable
	String uploadAsset(String fileName, String contentType, InputStream inputStream) throws IOException;
}
