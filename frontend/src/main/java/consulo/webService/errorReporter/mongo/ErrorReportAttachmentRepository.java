package consulo.webService.errorReporter.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import consulo.webService.errorReporter.domain.ErrorReportAttachment;

/**
 * @author VISTALL
 * @since 02-Oct-16
 */
public interface ErrorReportAttachmentRepository extends MongoRepository<ErrorReportAttachment, String>
{
}
