package consulo.hub.frontend.vflow.repository.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import consulo.hub.frontend.vflow.PropertiesServiceImpl;
import consulo.hub.frontend.vflow.backend.service.BackendPluginStatisticsService;
import consulo.hub.frontend.vflow.backend.service.BackendRepositoryService;
import consulo.hub.frontend.vflow.base.MainLayout;
import consulo.hub.frontend.vflow.repository.ui.RepositoryChannelPanel;
import consulo.hub.frontend.vflow.repository.ui.TagsLocalizeLoader;
import consulo.procoeton.core.vaadin.ui.VChildLayout;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author VISTALL
 * @since 12-Mar-17
 */
@Route(value = "repository/:id?", layout = MainLayout.class)
@PageTitle("Repository")
@AnonymousAllowed
public class RepositoryView extends VChildLayout {
    public static final String ID = "id";

    private final BackendRepositoryService myBackendRepositoryService;
    private final BackendPluginStatisticsService myBackendPluginStatisticsService;
    private final TagsLocalizeLoader myTagsLocalizeLoader;
    private final PropertiesServiceImpl myPropertiesService;

    private Component myLastRepositoryView;


    @Autowired
    public RepositoryView(
        PropertiesServiceImpl propertiesService,
        BackendRepositoryService backendRepositoryService,
        BackendPluginStatisticsService backendPluginStatisticsService,
        TagsLocalizeLoader tagsLocalizeLoader
    ) {
        myPropertiesService = propertiesService;
        myBackendRepositoryService = backendRepositoryService;
        myBackendPluginStatisticsService = backendPluginStatisticsService;
        myTagsLocalizeLoader = tagsLocalizeLoader;
    }

    @Override
    public void viewReady(AfterNavigationEvent afterNavigationEvent) {
        if (myPropertiesService.isNotInstalled()) {
            return;
        }

        String id = myRouteParameters.get(ID).orElse(null);

        rebuild(id);
    }

    private void rebuild(String id) {
        if (myLastRepositoryView != null) {
            remove(myLastRepositoryView);
        }

        RepositoryChannelPanel repositoryChannelPanel = new RepositoryChannelPanel(
            myBackendRepositoryService,
            myBackendPluginStatisticsService,
            myTagsLocalizeLoader,
            myRouteParameters
        );

        add(repositoryChannelPanel);

        repositoryChannelPanel.selectPlugin(id);

        myLastRepositoryView = repositoryChannelPanel;
    }
}
