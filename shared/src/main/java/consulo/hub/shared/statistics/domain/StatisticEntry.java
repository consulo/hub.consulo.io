package consulo.hub.shared.statistics.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import consulo.hub.shared.auth.domain.UserAccount;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author VISTALL
 * @since 28/08/2021
 */
@Entity
@JsonIgnoreProperties
public class StatisticEntry
{
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	private Long id;

	private String key;

	private String installationID;

	@OneToOne
	private UserAccount user;

	@OneToMany(cascade = CascadeType.ALL)
	@OrderColumn
	private List<StatisticUsageGroup> groups = new ArrayList<>();

	private long createTime;

	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public String getKey()
	{
		return key;
	}

	public void setKey(String key)
	{
		this.key = key;
	}

	public String getInstallationID()
	{
		return installationID;
	}

	public void setInstallationID(String installationID)
	{
		this.installationID = installationID;
	}

	public UserAccount getUser()
	{
		return user;
	}

	public void setUser(UserAccount user)
	{
		this.user = user;
	}

	public List<StatisticUsageGroup> getGroups()
	{
		return groups;
	}

	public void setGroups(List<StatisticUsageGroup> groups)
	{
		this.groups = groups;
	}

	public long getCreateTime()
	{
		return createTime;
	}

	public void setCreateTime(long createTime)
	{
		this.createTime = createTime;
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
		StatisticEntry that = (StatisticEntry) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(id);
	}
}
