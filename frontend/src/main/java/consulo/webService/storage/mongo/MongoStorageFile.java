package consulo.webService.storage.mongo;

import java.util.Objects;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author VISTALL
 * @since 18-Feb-17
 */
@Document(collection = "storageFile")
public class MongoStorageFile
{
	@Id
	private String id;

	@Indexed
	private String email;

	@Indexed
	private String filePath;

	private byte[] data;

	private int modCount;

	public MongoStorageFile()
	{
	}

	public MongoStorageFile(String id)
	{
		this.id = id;
	}

	public String getEmail()
	{
		return email;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
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

	public byte[] getData()
	{
		return data;
	}

	public void setData(byte[] data)
	{
		this.data = data;
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
		if(!(o instanceof MongoStorageFile))
		{
			return false;
		}
		MongoStorageFile that = (MongoStorageFile) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(id);
	}
}
