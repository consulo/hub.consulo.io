package consulo.hub.shared.statistics.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * @author VISTALL
 * @since 28/08/2021
 */
@Entity
public class StatisticUsageGroupValue
{
	@JsonIgnore
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	private Long id;

	@JsonProperty("id")
	public String usageGroupValueId;

	public int count;

	public StatisticUsageGroupValue()
	{
	}

	public StatisticUsageGroupValue(String usageGroupValueId, int count)
	{
		this.usageGroupValueId = usageGroupValueId;
		this.count = count;
	}

	public String getUsageGroupValueId()
	{
		return usageGroupValueId;
	}

	public void setUsageGroupValueId(String usageGroupValueId)
	{
		this.usageGroupValueId = usageGroupValueId;
	}

	public int getCount()
	{
		return count;
	}

	public void setCount(int count)
	{
		this.count = count;
	}

	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}
}
