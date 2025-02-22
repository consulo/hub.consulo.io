package consulo.hub.backend.repository.cron;

import consulo.hub.backend.repository.cleanup.RepositoryCleanupService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * @author VISTALL
 * @since 2025-02-09
 */
@Service
public class RepositoryCleanuper {

    private final RepositoryCleanupService myRepositoryChannelsService;

    public RepositoryCleanuper(RepositoryCleanupService repositoryChannelsService) {
        myRepositoryChannelsService = repositoryChannelsService;
    }

    @Scheduled(cron = "0 * * * * *")
    public void cleanup() {
        if (Boolean.FALSE) {
            myRepositoryChannelsService.runCleanUpAsync();
        }
    }
}
