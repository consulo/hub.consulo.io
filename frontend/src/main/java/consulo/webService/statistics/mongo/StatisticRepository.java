package consulo.webService.statistics.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import consulo.webService.statistics.domain.StatisticBean;

/**
 * @author VISTALL
 * @since 2020-05-31
 */
public interface StatisticRepository extends MongoRepository<StatisticBean, String>
{
}
