package consulo.app.plugins.frontend.backend.service;

import consulo.hub.shared.repository.FrontPluginNode;
import consulo.hub.shared.repository.FrontPluginNodeById;
import consulo.hub.shared.repository.PluginNode;
import consulo.procoeton.core.backend.ApiBackendRequestor;
import consulo.procoeton.core.backend.BackendApiUrl;
import consulo.procoeton.core.backend.BackendServiceDownException;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 2021-08-21
 */
@Service
public class BackendRepositoryService {
    private static final Logger LOG = LoggerFactory.getLogger(BackendRepositoryService.class);

    @Autowired
    private ApiBackendRequestor myApiBackendRequestor;

    public PluginNode[] listOldPlugins() {
        try {
            PluginNode[] nodes =
                myApiBackendRequestor.runRequest(BackendApiUrl.toPublic("/repository/list"), Map.of("channel", "nightly",
                    "platformVersion", "SNAPSHOT",
                    "addObsoletePlatformsV2", "false"
                ), PluginNode[].class);
            if (nodes == null) {
                nodes = new PluginNode[0];
            }

            return nodes;
        }
        catch (BackendServiceDownException e) {
            throw e;
        }
        catch (Exception e) {
            LOG.error("Fail to get plugins", e);
        }
        return new PluginNode[0];
    }

    public void listAll(@Nonnull Consumer<FrontPluginNodeById> consumer) {
        try {
            FrontPluginNodeById[] nodes =
                myApiBackendRequestor.runRequest(BackendApiUrl.toPrivate("/repository/listById"), Map.of(), FrontPluginNodeById[].class);
            if (nodes == null) {
                nodes = new FrontPluginNodeById[0];
            }

            for (FrontPluginNodeById node : nodes) {
                consumer.accept(node);
            }
        }
        catch (BackendServiceDownException e) {
            throw e;
        }
        catch (Exception e) {
            LOG.error("Fail to get plugins", e);
        }
    }
}
