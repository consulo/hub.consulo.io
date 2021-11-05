package consulo.hub.backend.repository;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import consulo.util.lang.StringUtil;

/**
 * @author VISTALL
 * @since 16/10/2021
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties
public class RestPluginHistoryEntry
{
	private String pluginVersion;

	private String repoUrl;

	//private String commitUrl;
	private String commitHash;
	private String commitMessage;
	private long commitTimestamp;
	private String commitAuthor;

	@JsonIgnore
	public boolean isEmpty()
	{
		if(!StringUtil.isEmptyOrSpaces(commitMessage) && commitTimestamp > 0)
		{
			return false;
		}
		return true;
	}

	public String getRepoUrl()
	{
		return repoUrl;
	}

	public void setRepoUrl(String repoUrl)
	{
		this.repoUrl = repoUrl;
	}

	public String getCommitHash()
	{
		return commitHash;
	}

	public void setCommitHash(String commitHash)
	{
		this.commitHash = commitHash;
	}

	public String getCommitMessage()
	{
		return commitMessage;
	}

	public void setCommitMessage(String commitMessage)
	{
		this.commitMessage = commitMessage;
	}

	public long getCommitTimestamp()
	{
		return commitTimestamp;
	}

	public void setCommitTimestamp(long commitTimestamp)
	{
		this.commitTimestamp = commitTimestamp;
	}

	public String getCommitAuthor()
	{
		return commitAuthor;
	}

	public void setCommitAuthor(String commitAuthor)
	{
		this.commitAuthor = commitAuthor;
	}

	public String getPluginVersion()
	{
		return pluginVersion;
	}

	public void setPluginVersion(String pluginVersion)
	{
		this.pluginVersion = pluginVersion;
	}
}
