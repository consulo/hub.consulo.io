package consulo.hub.frontend.vflow;

import com.google.common.annotations.VisibleForTesting;
import consulo.procoeton.core.BaseProPropertiesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author VISTALL
 * @since 2016-08-28
 */
@Service
public class PropertiesServiceImpl extends BaseProPropertiesService {
    @Autowired
    public PropertiesServiceImpl() {
        this(null);
    }

    @VisibleForTesting
    public PropertiesServiceImpl(String home) {
        super(home, ".hub-frontend");
    }
}
