package consulo.hub.backend.statistics.repository;

import consulo.hub.shared.statistics.domain.StatisticEntry;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author VISTALL
 * @since 28/08/2021
 */
public interface StatisticEntryRepository extends JpaRepository<StatisticEntry, Long>
{
}
