package consulo.webService.auth.mongo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import consulo.webService.auth.mongo.domain.Role;

public interface RoleRepository extends MongoRepository<Role, String>
{
}
