package consulo.hub.shared.repository.mongo.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author VISTALL
 * @since 04-Jan-17
 */
@Document(collection = "repositoryNode")
public class MongoPluginNode
{
	@Id
	private String id;

	@DBRef
	private List<MongoDownloadStat> downloadStat = new ArrayList<>();

	public MongoPluginNode()
	{
	}

	public MongoPluginNode(String id)
	{
		this.id = id;
	}

	public List<MongoDownloadStat> getDownloadStat()
	{
		return downloadStat;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	@Override
	public boolean equals(Object o)
	{
		if(this == o)
		{
			return true;
		}
		if(!(o instanceof MongoPluginNode))
		{
			return false;
		}
		MongoPluginNode that = (MongoPluginNode) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(id);
	}
}
