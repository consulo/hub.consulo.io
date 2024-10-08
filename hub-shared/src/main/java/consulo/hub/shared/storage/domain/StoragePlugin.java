package consulo.hub.shared.storage.domain;

import consulo.hub.shared.auth.domain.UserAccount;
import jakarta.persistence.*;

import java.util.Objects;

/**
 * @author VISTALL
 * @since 13/09/2021
 */
@Entity
@Table(indexes = {
		// user_id is id for User
		@Index(columnList = "user_id, pluginId", unique = true)
})
public class StoragePlugin
{
	@Id
	@GeneratedValue
	@Column
	private Long id;

	@OneToOne
	private UserAccount user;

	private String pluginId;

	private StoragePluginState pluginState = StoragePluginState.ENABLED;

	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public UserAccount getUser()
	{
		return user;
	}

	public void setUser(UserAccount user)
	{
		this.user = user;
	}

	public String getPluginId()
	{
		return pluginId;
	}

	public void setPluginId(String pluginId)
	{
		this.pluginId = pluginId;
	}

	public StoragePluginState getPluginState()
	{
		return pluginState;
	}

	public void setPluginState(StoragePluginState pluginState)
	{
		this.pluginState = pluginState;
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
		StoragePlugin that = (StoragePlugin) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(id);
	}

	@Override
	public String toString()
	{
		return "StoragePlugin{" +
				"id=" + id +
				", user=" + user +
				", pluginId='" + pluginId + '\'' +
				", pluginState=" + pluginState +
				'}';
	}
}
