package consulo.hub.shared.statistics.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.UUID;

/**
 * @author VISTALL
 * @since 2020-05-31
 */
@Document(collection = "statistic")
@JsonIgnoreProperties
public class StatisticBean implements Serializable
{
	public static class UsageGroup implements Serializable
	{
		public String id;

		public UsageGroupValue[] values = new UsageGroupValue[0];

		public String getId()
		{
			return id;
		}

		public void setId(String id)
		{
			this.id = id;
		}

		public UsageGroupValue[] getValues()
		{
			return values;
		}

		public void setValues(UsageGroupValue[] values)
		{
			this.values = values;
		}
	}

	public static class UsageGroupValue implements Serializable
	{
		public String id;

		public int count;

		public UsageGroupValue()
		{
		}

		public UsageGroupValue(String id, int count)
		{
			this.id = id;
			this.count = count;
		}

		public String getId()
		{
			return id;
		}

		public void setId(String id)
		{
			this.id = id;
		}

		public int getCount()
		{
			return count;
		}

		public void setCount(int count)
		{
			this.count = count;
		}
	}

	@Id
	private final String id = UUID.randomUUID().toString();

	@Indexed
	public String key;

	@Indexed
	public String installationID;

	@Indexed
	public String ownerEmail;

	public UsageGroup[] groups = new UsageGroup[0];

	private long createTime;

	public String getId()
	{
		return id;
	}

	public String getInstallationID()
	{
		return installationID;
	}

	public void setInstallationID(String installationID)
	{
		this.installationID = installationID;
	}

	public UsageGroup[] getGroups()
	{
		return groups;
	}

	public void setGroups(UsageGroup[] groups)
	{
		this.groups = groups;
	}

	public String getKey()
	{
		return key;
	}

	public void setKey(String key)
	{
		this.key = key;
	}

	public void setOwnerEmail(String ownerEmail)
	{
		this.ownerEmail = ownerEmail;
	}

	public String getOwnerEmail()
	{
		return ownerEmail;
	}

	public void setCreateTime(long createTime)
	{
		this.createTime = createTime;
	}

	public long getCreateTime()
	{
		return createTime;
	}
}
