package consulo.hub.shared.storage.domain;

import consulo.hub.shared.base.InformationBean;
import jakarta.persistence.*;

/**
 * @author VISTALL
 * @since 20/08/2021
 */
@Entity
public class StorageFileUpdateBy extends InformationBean
{
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	private Long id;

	@Column
	private long time;

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
}
