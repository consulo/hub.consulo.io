package consulo.hub.shared.repository.domain;

import javax.persistence.*;
import java.util.Objects;

/**
 * @author VISTALL
 * @since 28/08/2021
 */
@Entity
@Table(
		indexes = @Index(columnList = "pluginId")
)
public class RepositoryDownloadInfo
{
	@Id
	@GeneratedValue
	private Long id;

	private long time;
	private String pluginId;
	private String channel;
	private String version;
	private String platformVersion;
	private boolean viaUpdate;

	public RepositoryDownloadInfo()
	{
	}

	public RepositoryDownloadInfo(long time, String pluginId, String channel, String version, String platformVersion, boolean viaUpdate)
	{
		this.time = time;
		this.pluginId = pluginId;
		this.channel = channel;
		this.version = version;
		this.platformVersion = platformVersion;
		this.viaUpdate = viaUpdate;
	}

	public long getTime()
	{
		return time;
	}

	public void setTime(long time)
	{
		this.time = time;
	}

	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public String getPluginId()
	{
		return pluginId;
	}

	public void setPluginId(String pluginId)
	{
		this.pluginId = pluginId;
	}

	public String getChannel()
	{
		return channel;
	}

	public void setChannel(String channel)
	{
		this.channel = channel;
	}

	public String getVersion()
	{
		return version;
	}

	public void setVersion(String version)
	{
		this.version = version;
	}

	public String getPlatformVersion()
	{
		return platformVersion;
	}

	public void setPlatformVersion(String platformVersion)
	{
		this.platformVersion = platformVersion;
	}

	public boolean getViaUpdate()
	{
		return viaUpdate;
	}

	public void setViaUpdate(boolean viaUpdate)
	{
		this.viaUpdate = viaUpdate;
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
		RepositoryDownloadInfo that = (RepositoryDownloadInfo) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(id);
	}
}
