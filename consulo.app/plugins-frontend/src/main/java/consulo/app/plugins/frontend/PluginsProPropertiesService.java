package consulo.app.plugins.frontend;

import consulo.procoeton.core.BaseProPropertiesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author VISTALL
 * @since 2025-05-10
 */
@Service
public class PluginsProPropertiesService extends BaseProPropertiesService {
    @Autowired
    public PluginsProPropertiesService() {
        this(null);
    }

    public PluginsProPropertiesService(String home) {
        super(home, ".config");
    }
}
