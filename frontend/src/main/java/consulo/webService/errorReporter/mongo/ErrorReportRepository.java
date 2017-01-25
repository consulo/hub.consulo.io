package consulo.webService.errorReporter.mongo;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import consulo.webService.errorReporter.domain.ErrorReport;

/**
 * @author VISTALL
 * @since 02-Oct-16
 */
public interface ErrorReportRepository extends MongoRepository<ErrorReport, String>
{
	String CREATE_DATE = "createDate";

	List<ErrorReport> findByReporterEmail(String email, Sort sort);

	List<ErrorReport> findByReporterEmail(String email, Pageable pageable);
}
