package consulo.hub.shared.storage.domain;

import consulo.hub.shared.auth.domain.UserAccount;
import jakarta.persistence.*;

import java.util.Objects;

/**
 * @author VISTALL
 * @since 20/08/2021
 */
@Entity
@Table
public class StorageFile
{
	@Id
	@GeneratedValue
	@Column
	private Long id;

	@OneToOne
	private UserAccount user;

	@Column(nullable = false)
	private String filePath;

	@Column(nullable = false)
	private byte[] fileData;

	@OneToOne(cascade = CascadeType.ALL)
	private StorageFileUpdateBy updateBy;

	@Column
	private int modCount;

	public StorageFileUpdateBy getUpdateBy()
	{
		return updateBy;
	}

	public void setUpdateBy(StorageFileUpdateBy updateBy)
	{
		this.updateBy = updateBy;
	}

	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public String getFilePath()
	{
		return filePath;
	}

	public void setFilePath(String filePath)
	{
		this.filePath = filePath;
	}

	public byte[] getFileData()
	{
		return fileData;
	}

	public void setFileData(byte[] fileData)
	{
		this.fileData = fileData;
	}

	public UserAccount getUser()
	{
		return user;
	}

	public void setUser(UserAccount user)
	{
		this.user = user;
	}

	public int getModCount()
	{
		return modCount;
	}

	public void setModCount(int modCount)
	{
		this.modCount = modCount;
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
		StorageFile that = (StorageFile) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(id);
	}
}
