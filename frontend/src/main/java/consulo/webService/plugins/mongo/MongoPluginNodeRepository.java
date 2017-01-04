package consulo.webService.plugins.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author VISTALL
 * @since 04-Jan-17
 */
public interface MongoPluginNodeRepository extends MongoRepository<MongoPluginNode, String>
{
}
