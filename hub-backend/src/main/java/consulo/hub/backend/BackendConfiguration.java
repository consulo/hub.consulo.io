package consulo.hub.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import consulo.hub.backend.github.release.GithubReleaseService;
import consulo.hub.backend.github.release.impl.GithubReleaseServiceImpl;
import consulo.hub.backend.impl.AsyncTempFileServiceImpl;
import consulo.hub.backend.impl.WorkDirectoryServiceImpl;
import consulo.hub.backend.repository.analyzer.PluginAnalyzerRunnerFactory;
import consulo.hub.backend.repository.analyzer.builtin.BuiltinPluginAnalyzerRunnerFactory;
import consulo.hub.backend.repository.impl.store.neww.NewRepositoryChannelsService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.MultipartConfigElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

/**
 * @author VISTALL
 * @since 01/05/2023
 */
@Configuration
public class BackendConfiguration {
    @Autowired
    private ObjectMapper objectMapper;

    @Bean
    public StandardServletMultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    @Bean
    public GithubReleaseService githubReleaseService(@Value("${github.oauth2.token:}") String oauthToken) {
        return new GithubReleaseServiceImpl(oauthToken);
    }

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        int _256mb = 256 * 1024 * 1024;
        return new MultipartConfigElement(null, _256mb, _256mb, -1);
    }

    @Bean
    public TaskExecutor taskExecutor() {
        return new ThreadPoolTaskExecutor();
    }

    @Bean
    public TaskScheduler taskScheduler() {
        return new ThreadPoolTaskScheduler();
    }

    @Bean
    public WorkDirectoryService repositoryWorkDirectoryService(@Value("${working.directory:hub-workdir}") String workingDirectoryPath) {
        return new WorkDirectoryServiceImpl(workingDirectoryPath);
    }

    @Bean
    @Order(1_000)
    public NewRepositoryChannelsService pluginChannelsService(WorkDirectoryService workDirectoryService, AsyncTempFileServiceImpl fileService, TaskExecutor taskExecutor) {
        return new NewRepositoryChannelsService(workDirectoryService, fileService, taskExecutor);
    }

    @Bean
    public PluginAnalyzerRunnerFactory pluginAnalyzerRunnerFactory(ObjectMapper objectMapper) {
        return new BuiltinPluginAnalyzerRunnerFactory(objectMapper);
    }

    @PostConstruct
    public void setup() {
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }
}
