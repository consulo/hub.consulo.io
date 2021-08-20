package consulo.hub.backend.repository.mongo;

import consulo.hub.shared.repository.mongo.domain.MongoPluginNode;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author VISTALL
 * @since 04-Jan-17
 */
public interface MongoPluginNodeRepository extends MongoRepository<MongoPluginNode, String>
{
}
