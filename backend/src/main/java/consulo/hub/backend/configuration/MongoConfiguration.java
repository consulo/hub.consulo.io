package consulo.hub.backend.configuration;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ReadPreference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * @author VISTALL
 * @since 22/08/2021
 */
@Configuration
public class MongoConfiguration
{
	@Value("${mongo.host:localhost}")
	private String myMongoHostName;

	@Bean
	public MongoClient mongo()
	{
		return new MongoClient(myMongoHostName, MongoClientOptions.builder().readPreference(ReadPreference.secondary()).build());
	}

	@Bean
	public MongoTemplate mongoTemplate() throws Exception
	{
		return new MongoTemplate(mongo(), "auth");
	}
}
