package consulo.hub.shared.repository.mongo.domain;

import consulo.hub.shared.repository.PluginChannel;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Objects;
import java.util.UUID;

/**
 * @author VISTALL
 * @since 04-Jan-17
 */
@Document(collection = "repositoryNodeDownloadStat")
public class MongoDownloadStat
{
	@Id
	private String id;

	private long time;
	private String channel;
	private String version;
	private String platformVersion;
	private Boolean viaUpdate;

	public MongoDownloadStat()
	{
	}

	public MongoDownloadStat(long timeMillis, PluginChannel channel, String version, String platformVersion)
	{
		this.id = UUID.randomUUID().toString();
		this.time = timeMillis;
		this.channel = channel.name();
		this.version = version;
		this.platformVersion = platformVersion;
	}

	public void setViaUpdate(Boolean viaUpdate)
	{
		this.viaUpdate = viaUpdate;
	}

	public Boolean getViaUpdate()
	{
		return viaUpdate;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public void setChannel(String channel)
	{
		this.channel = channel;
	}

	public String getChannel()
	{
		return channel;
	}

	public String getVersion()
	{
		return version;
	}

	public void setVersion(String version)
	{
		this.version = version;
	}

	public void setPlatformVersion(String platformVersion)
	{
		this.platformVersion = platformVersion;
	}

	public String getPlatformVersion()
	{
		return platformVersion;
	}

	public void setTime(long time)
	{
		this.time = time;
	}

	public long getTime()
	{
		return time;
	}

	@Override
	public boolean equals(Object o)
	{
		if(this == o)
		{
			return true;
		}
		if(!(o instanceof MongoDownloadStat))
		{
			return false;
		}
		MongoDownloadStat that = (MongoDownloadStat) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(id);
	}
}
