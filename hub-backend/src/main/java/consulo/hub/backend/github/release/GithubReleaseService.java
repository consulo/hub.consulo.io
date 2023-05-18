package consulo.hub.backend.github.release;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 17/05/2023
 */
public interface GithubReleaseService
{
	@Nonnull
	GithubRelease createTagAndRelease(String repoUrl, String commitHash, GithubTagBuilder tagBuilder) throws Exception;
}
