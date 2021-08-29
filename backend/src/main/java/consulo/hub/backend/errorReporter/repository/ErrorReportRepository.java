package consulo.hub.backend.errorReporter.repository;

import consulo.hub.shared.auth.domain.UserAccount;
import consulo.hub.shared.errorReporter.domain.ErrorReport;
import consulo.hub.shared.errorReporter.domain.ErrorReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author VISTALL
 * @since 28/08/2021
 */
public interface ErrorReportRepository extends JpaRepository<ErrorReport, Long>
{
	ErrorReport findByLongId(String longId);

	Page<ErrorReport> findByStatusIn(ErrorReportStatus[] statuses, Pageable pageable);

	Page<ErrorReport> findByUserAndStatusIn(UserAccount account, ErrorReportStatus[] statuses, Pageable pageable);
}
