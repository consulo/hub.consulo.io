package consulo.hub.backend.errorReporter.mongo;

import consulo.hub.shared.errorReporter.domain.ErrorReport;
import consulo.hub.shared.errorReporter.domain.ErrorReporterStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 02-Oct-16
 */
public interface ErrorReportRepository extends MongoRepository<ErrorReport, String>
{
	String CREATE_DATE = "createDate";

	@Nonnull
	Page<ErrorReport> findByReporterEmail(String email, Pageable pageable);

	@Nonnull
	Page<ErrorReport> findByReporterEmailAndStatusIn(String email, ErrorReporterStatus[] errorReporterStatuses, Pageable pageable);

	@Nonnull
	Page<ErrorReport> findByStatusIn(ErrorReporterStatus[] errorReporterStatuses, Pageable pageable);
}
