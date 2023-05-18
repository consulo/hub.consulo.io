package consulo.hub.backend.github.release.impl;

import consulo.hub.backend.github.release.GithubRelease;
import jakarta.annotation.Nullable;

import java.io.InputStream;

/**
 * @author VISTALL
 * @since 18/05/2023
 */
public class StubGithubReleaseImpl implements GithubRelease
{
	public static final GithubRelease INSTANCE = new StubGithubReleaseImpl();

	@Nullable
	@Override
	public String uploadAsset(String fileName, String contentType, InputStream inputStream)
	{
		return null;
	}
}
