package consulo.webService.storage.domain;

import consulo.webService.util.InformationBean;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * @author VISTALL
 * @since 20/08/2021
 */
@Entity
public class StorageFileUpdateBy extends InformationBean
{
	@Id
	@GeneratedValue
	private Integer id;

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

	public Integer getId()
	{
		return id;
	}

	public void setId(Integer id)
	{
		this.id = id;
	}
}
