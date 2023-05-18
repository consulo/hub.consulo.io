package consulo.hub.backend.github.release;

import consulo.hub.shared.repository.PluginNode;
import consulo.hub.shared.repository.util.RepositoryUtil;

/**
 * @author VISTALL
 * @since 18/05/2023
 */
public class RepoGithubTagBuilder implements GithubTagBuilder
{
	private final PluginNode myPluginNode;

	public RepoGithubTagBuilder(PluginNode pluginNode)
	{
		myPluginNode = pluginNode;
	}

	@Override
	public String buildTagName()
	{
		String tagName;
		if(RepositoryUtil.isPlatformNode(myPluginNode.id))
		{
			tagName = String.format("consulo#%s", myPluginNode.version);
		}
		else
		{
			tagName = String.format("build#%s_consulo#%s", myPluginNode.version, myPluginNode.platformVersion);
		}

		return tagName;
	}

	@Override
	public String buildReleaseName()
	{
		String releaseName;
		String version = myPluginNode.version;
		String platformVersion = myPluginNode.platformVersion;

		if(RepositoryUtil.isPlatformNode(myPluginNode.id))
		{
			releaseName = String.format("Consulo #%s", version);
		}
		else
		{
			releaseName = String.format("Build #%s [Consulo #%s]", version, platformVersion);
		}

		return releaseName;
	}
}
