package consulo.hub.backend.errorReporter.repository;

import consulo.hub.shared.errorReporter.domain.ErrorReport;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author VISTALL
 * @since 28/08/2021
 */
public interface ErrorReportRepository extends JpaRepository<ErrorReport, Integer>
{
}
