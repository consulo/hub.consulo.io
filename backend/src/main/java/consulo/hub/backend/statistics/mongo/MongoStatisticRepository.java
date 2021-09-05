package consulo.hub.backend.statistics.mongo;

import consulo.hub.shared.statistics.domain.MongoStatisticBean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author VISTALL
 * @since 2020-05-31
 */
@Profile("mongo")
public interface MongoStatisticRepository extends MongoRepository<MongoStatisticBean, String>
{
}
