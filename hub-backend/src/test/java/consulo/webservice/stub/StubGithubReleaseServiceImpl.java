package consulo.webservice.stub;

import consulo.hub.backend.github.release.GithubRelease;
import consulo.hub.backend.github.release.GithubReleaseService;
import consulo.hub.backend.github.release.GithubTagBuilder;
import consulo.hub.backend.github.release.impl.StubGithubReleaseImpl;
import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 21/05/2023
 */
public class StubGithubReleaseServiceImpl implements GithubReleaseService {
    @Nonnull
    @Override
    public GithubRelease createTagAndRelease(String repoUrl, String commitHash, GithubTagBuilder tagBuilder) throws Exception {
        return StubGithubReleaseImpl.INSTANCE;
    }
}
