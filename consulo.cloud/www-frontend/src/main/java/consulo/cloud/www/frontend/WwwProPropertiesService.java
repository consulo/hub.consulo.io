package consulo.cloud.www.frontend;

import consulo.procoeton.core.BaseProPropertiesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author VISTALL
 * @since 2025-05-15
 */
@Service
public class WwwProPropertiesService extends BaseProPropertiesService {
    @Autowired
    public WwwProPropertiesService() {
        this(null);
    }

    public WwwProPropertiesService(String home) {
        super(home, ".config");
    }
}
