package consulo.hub.backend.configuration;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

/**
 * @author VISTALL
 * @since 06/09/2021
 */
@Configuration
@Profile("mongo")
@ConditionalOnClass(MongoClient.class)
@EnableConfigurationProperties(MongoProperties.class)
@ConditionalOnMissingBean(type = "org.springframework.data.mongodb.MongoDbFactory")
public class OverrideMongoAutoConfiguration extends MongoAutoConfiguration
{
	public OverrideMongoAutoConfiguration(MongoProperties properties,
										  ObjectProvider<MongoClientOptions> options,
										  Environment environment)
	{
		super(properties, options, environment);
	}
}
