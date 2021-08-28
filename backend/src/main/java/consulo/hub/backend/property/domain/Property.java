package consulo.hub.backend.property.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;

/**
 * @author VISTALL
 * @since 28/08/2021
 */
@Entity
@Table
public class Property
{
	@Id
	private String key;

	private String value;

	public String getKey()
	{
		return key;
	}

	public void setKey(String key)
	{
		this.key = key;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
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
		Property property = (Property) o;
		return Objects.equals(key, property.key);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(key);
	}
}
