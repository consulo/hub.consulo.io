package consulo.hub.backend.repository.mongo;

import consulo.hub.shared.repository.mongo.domain.MongoPluginNode;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author VISTALL
 * @since 04-Jan-17
 */
@Profile("mongo")
public interface MongoPluginNodeRepository extends MongoRepository<MongoPluginNode, String>
{
}
