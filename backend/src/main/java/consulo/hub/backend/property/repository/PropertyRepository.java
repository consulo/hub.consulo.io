package consulo.hub.backend.property.repository;

import consulo.hub.backend.property.domain.Property;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author VISTALL
 * @since 28/08/2021
 */
public interface PropertyRepository extends JpaRepository<Property, String>
{
}
