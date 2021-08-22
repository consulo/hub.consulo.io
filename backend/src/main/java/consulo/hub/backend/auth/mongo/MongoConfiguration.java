package consulo.hub.backend.auth.mongo;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;

/**
 * @author VISTALL
 * @since 26-Sep-16
 */
@Configuration
public class MongoConfiguration extends AbstractMongoConfiguration
{
	@Value("${mongo.host:localhost}")
	private String myMongoHostName;

	@Override
	protected String getDatabaseName()
	{
		return "auth";
	}

	@Override
	public Mongo mongo() throws Exception
	{
		return new MongoClient(myMongoHostName);
	}
}
