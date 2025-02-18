package consulo.hub.backend.repository.cron;

import consulo.hub.backend.repository.RepositoryChannelIterationService;
import consulo.hub.backend.repository.RepositoryChannelsService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * @author VISTALL
 * @since 2025-02-09
 */
@Service
public class PluginsCleanuper {

    private final RepositoryChannelsService myRepositoryChannelsService;
    private final RepositoryChannelIterationService myRepositoryChannelIterationService;

    public PluginsCleanuper(RepositoryChannelsService repositoryChannelsService,
                                            RepositoryChannelIterationService repositoryChannelIterationService) {
        myRepositoryChannelsService = repositoryChannelsService;
        myRepositoryChannelIterationService = repositoryChannelIterationService;
    }

    @Scheduled(cron = "0 * * * * *")
    public void cleanup() {
        // TODO
    }
}
