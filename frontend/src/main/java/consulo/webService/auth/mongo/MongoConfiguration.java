package consulo.webService.auth.mongo;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

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
		try
		{
			Context initCtx = new InitialContext();
			return (MongoClient) initCtx.lookup("java:/comp/env/mongodb/hub");
		}
		catch(NamingException e)
		{
			return new MongoClient("localhost");
		}
	}
}
