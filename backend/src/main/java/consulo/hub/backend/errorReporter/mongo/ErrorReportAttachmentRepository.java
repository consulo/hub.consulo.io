package consulo.hub.backend.errorReporter.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import consulo.hub.shared.errorReporter.domain.ErrorReportAttachment;

/**
 * @author VISTALL
 * @since 02-Oct-16
 */
public interface ErrorReportAttachmentRepository extends MongoRepository<ErrorReportAttachment, String>
{
}
