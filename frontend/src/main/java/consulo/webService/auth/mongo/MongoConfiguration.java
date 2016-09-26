package consulo.webService.auth.mongo;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;

/**
 * @author VISTALL
 * @since 26-Sep-16
 */
@Configuration
public class MongoConfiguration extends AbstractMongoConfiguration
{
	@Override
	protected String getDatabaseName()
	{
		return "auth";
	}

	@Override
	public Mongo mongo() throws Exception
	{
		return new MongoClient("localhost");
	}
}
