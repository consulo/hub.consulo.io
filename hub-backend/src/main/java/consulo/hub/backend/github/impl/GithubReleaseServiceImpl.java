package consulo.hub.backend.github.impl;

import consulo.hub.backend.github.GithubReleaseService;
import consulo.hub.shared.repository.PluginNode;
import org.kohsuke.github.*;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author VISTALL
 * @since 17/05/2023
 */
public class GithubReleaseServiceImpl implements GithubReleaseService
{
	private final GitHub gitHub;

	public GithubReleaseServiceImpl()
	{
		try
		{
			gitHub = GitHub.connectUsingOAuth("");
		}
		catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}


	@Override
	public String createTagAndRelease(String repoUrl, String commitHash, String version, String platformVersion, PluginNode pluginNode)  throws Exception
	{
		// https://github.com/consulo/consulo-spellchecker/
		String repoName = repoUrl.replace("https://github.com/", "");
		if(repoName.endsWith("/"))
		{
			repoName = repoName.substring(0, repoName.length() - 1);
		}

		GHRepository repository = gitHub.getRepository(repoName);

		String tagName = String.format("build#%s_consulo#%s", version, platformVersion);
		String releaseName = String.format("Build #%s [Consulo #%s]", version, platformVersion);

		GHTagObject tagObject = repository.createTag(tagName, releaseName, commitHash, "commit");

		GHReleaseBuilder release = repository.createRelease(tagObject.getTag());
		release.name(releaseName);

		GHRelease ghRelease = release.create();

		try (FileInputStream stream = new FileInputStream(pluginNode.targetFile))
		{
			String fileName = pluginNode.id + "_" + pluginNode.version + ".consulo-plugin";

			GHAsset asset = ghRelease.uploadAsset(fileName, stream, "application/zip");
			String browserDownloadUrl = asset.getBrowserDownloadUrl();

			return browserDownloadUrl;
		}
	}
}
