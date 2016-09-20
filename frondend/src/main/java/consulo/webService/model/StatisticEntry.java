package consulo.webService.model;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

/**
 * @author VISTALL
 * @since 21.04.14
 */
@Entity
@Table(name = "consulo_statistics")
public class StatisticEntry
{
	@Id
	@Column(name = "uuid")
	@Type(type="uuid-char")
	private UUID myUUID;

	@ElementCollection
	@JoinTable(name = "consulo_statistics_values")
	private Map<String, Long> myValues;

	public StatisticEntry()
	{
	}

	public StatisticEntry(UUID uuid)
	{
		myUUID = uuid;
		myValues = new HashMap<String, Long>();
	}

	public void set(Map<String, Long> map)
	{
		for(Map.Entry<String, Long> entry : map.entrySet())
		{
			myValues.put(entry.getKey(), entry.getValue());
		}
	}

	public UUID getUUID()
	{
		return myUUID;
	}

	public Map<String, Long> getValues()
	{
		return myValues;
	}
}
