package consulo.hub.backend.github;

import consulo.hub.shared.repository.PluginNode;

/**
 * @author VISTALL
 * @since 17/05/2023
 */
public interface GithubReleaseService
{
	String createTagAndRelease(String repoUrl, String commitHash, String version, String platformVersion, PluginNode pluginNode) throws Exception;
}
