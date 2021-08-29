package consulo.hub.shared.statistics.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @since 28/08/2021
 */
@Entity
public class StatisticUsageGroup
{
	@JsonIgnore
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	private Long id;

	@JsonProperty("id")
	private String usageGroupId;

	@OneToMany(cascade = CascadeType.ALL)
	@OrderColumn
	private List<StatisticUsageGroupValue> values = new ArrayList<>();

	public String getUsageGroupId()
	{
		return usageGroupId;
	}

	public void setUsageGroupId(String usageGroupId)
	{
		this.usageGroupId = usageGroupId;
	}

	public List<StatisticUsageGroupValue> getValues()
	{
		return values;
	}

	public void setValues(List<StatisticUsageGroupValue> values)
	{
		this.values = values;
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
