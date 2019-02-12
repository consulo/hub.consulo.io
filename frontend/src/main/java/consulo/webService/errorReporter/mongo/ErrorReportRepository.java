package consulo.webService.errorReporter.mongo;

import javax.annotation.Nonnull;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import consulo.webService.errorReporter.domain.ErrorReport;
import consulo.webService.errorReporter.domain.ErrorReporterStatus;

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
