package consulo.hub.backend.configuration;

import com.mongodb.Mongo;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * @author VISTALL
 * @since 06/09/2021
 */
@Profile("mongo")
@Configuration
@ConditionalOnClass({
		Mongo.class,
		MongoTemplate.class
})
@EnableConfigurationProperties(MongoProperties.class)
@AutoConfigureAfter(OverrideMongoAutoConfiguration.class)
public class OverrideMongoDataAutoConfiguration extends MongoDataAutoConfiguration
{
	public OverrideMongoDataAutoConfiguration(ApplicationContext applicationContext, MongoProperties properties)
	{
		super(applicationContext, properties);
	}
}
