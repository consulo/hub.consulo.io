package consulo.hub.backend.github.release.impl;

import consulo.hub.backend.github.release.GithubRelease;
import jakarta.annotation.Nullable;
import org.kohsuke.github.GHAsset;
import org.kohsuke.github.GHRelease;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author VISTALL
 * @since 18/05/2023
 */
public class GithubReleaseImpl implements GithubRelease
{
	private final GHRelease myGhRelease;

	public GithubReleaseImpl(GHRelease ghRelease)
	{
		myGhRelease = ghRelease;
	}

	@Nullable
	@Override
	public String uploadAsset(String fileName, String contentType, InputStream inputStream) throws IOException
	{
		GHAsset asset = myGhRelease.uploadAsset(fileName, inputStream, contentType);
		String browserDownloadUrl = asset.getBrowserDownloadUrl();

		return browserDownloadUrl;
	}
}
