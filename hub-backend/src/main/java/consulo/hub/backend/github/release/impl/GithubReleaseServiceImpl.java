package consulo.hub.backend.github.release.impl;

import consulo.hub.backend.ConfigChangedEvent;
import consulo.hub.backend.github.release.GithubRelease;
import consulo.hub.backend.github.release.GithubReleaseService;
import consulo.hub.backend.github.release.GithubTagBuilder;
import consulo.util.lang.StringUtil;
import jakarta.annotation.Nonnull;
import org.kohsuke.github.*;
import org.springframework.context.ApplicationListener;

import java.io.IOException;
import java.util.Objects;

/**
 * @author VISTALL
 * @since 17/05/2023
 */
public class GithubReleaseServiceImpl implements GithubReleaseService, ApplicationListener<ConfigChangedEvent>
{
	private GitHub gitHub;

	private final String myOAuth2Token;

	public GithubReleaseServiceImpl(String oauthToken)
	{
		myOAuth2Token = oauthToken;
	}

	@Override
	@Nonnull
	public GithubRelease createTagAndRelease(String repoUrl, String commitHash, GithubTagBuilder tagBuilder) throws Exception
	{
		if(StringUtil.isEmptyOrSpaces(myOAuth2Token))
		{
			return StubGithubReleaseImpl.INSTANCE;
		}

		// https://github.com/consulo/consulo-spellchecker/
		String repoName = repoUrl.replace("https://github.com/", "").trim();
		if(repoName.endsWith("/"))
		{
			repoName = repoName.substring(0, repoName.length() - 1);
		}

		GHRepository repository = gitHub().getRepository(repoName);

		String tagName = tagBuilder.buildTagName();
		String releaseName = tagBuilder.buildReleaseName();

		// try to search release by tag - if not found create it
		GHRelease ghRelease = repository.getReleaseByTagName(tagName);
		if(ghRelease == null)
		{
			GHTagObject tagObject = repository.createTag(tagName, releaseName, commitHash, "commit");

			GHReleaseBuilder release = repository.createRelease(tagObject.getTag());
			release.name(releaseName);

			ghRelease = release.create();
		}

		return new GithubReleaseImpl(Objects.requireNonNull(ghRelease));
	}

	private GitHub gitHub() throws IOException
	{
		if(gitHub == null)
		{
			gitHub = GitHub.connectUsingOAuth(myOAuth2Token);
		}

		return gitHub;
	}

	@Override
	public void onApplicationEvent(ConfigChangedEvent event)
	{
		gitHub = null;
	}
}
