package consulo.hub.backend.statistics.repository;

import consulo.hub.shared.auth.domain.UserAccount;
import consulo.hub.shared.statistics.domain.StatisticEntry;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author VISTALL
 * @since 28/08/2021
 */
public interface StatisticEntryRepository extends JpaRepository<StatisticEntry, Long>
{
	List<StatisticEntry> findByUser(UserAccount user, Sort sort);
}
