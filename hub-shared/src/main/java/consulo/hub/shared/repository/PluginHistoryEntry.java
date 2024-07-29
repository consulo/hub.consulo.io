package consulo.hub.shared.repository;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import consulo.hub.shared.auth.domain.UserAccount;
import jakarta.persistence.*;

import java.util.Objects;

/**
 * @author VISTALL
 * @since 16/10/2021
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties
@Entity
@Table(indexes = {
		@Index(columnList = "pluginId, pluginVersion")
})
public class PluginHistoryEntry
{
	@Id
	@GeneratedValue
	private Long id;

	@Column(nullable = false)
	private String pluginId;

	@Column(nullable = false)
	private String pluginVersion;

	private String repoUrl;
	private String commitHash;
	@Column(length = 1024)
	private String commitMessage;
	private long commitTimestamp;
	private String commitAuthor;

	@OneToOne
	private UserAccount deployUser;

	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
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

	public String getPluginId()
	{
		return pluginId;
	}

	public void setPluginId(String pluginId)
	{
		this.pluginId = pluginId;
	}

	public UserAccount getDeployUser()
	{
		return deployUser;
	}

	public void setDeployUser(UserAccount deployUser)
	{
		this.deployUser = deployUser;
	}

	@Override
	public boolean equals(Object o)
	{
		if(this == o)
		{
			return true;
		}
		if(o == null || getClass() != o.getClass())
		{
			return false;
		}
		PluginHistoryEntry that = (PluginHistoryEntry) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(id);
	}
}
