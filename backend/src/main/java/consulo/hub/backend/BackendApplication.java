package consulo.hub.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import consulo.hub.backend.auth.oauth2.service.UserAccountDetailsService;
import consulo.hub.backend.auth.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.websocket.WebSocketAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import javax.annotation.PostConstruct;
import javax.servlet.MultipartConfigElement;

@EnableScheduling
@EnableJpaRepositories
@SpringBootApplication(exclude = {
		WebSocketAutoConfiguration.class
})
@EnableGlobalMethodSecurity(securedEnabled = true)
@EntityScan({
		"consulo.hub.shared.*",
		"consulo.hub.backend.*"
})
public class BackendApplication
{
	@Configuration
	public static class Setup
	{
		@Autowired
		private ObjectMapper objectMapper;

		@Autowired
		public UserAccountRepository myUserAccountRepository;

		@Bean
		public UserDetailsService userDetailsService()
		{
			return new UserAccountDetailsService(myUserAccountRepository);
		}

		@Bean
		public StandardServletMultipartResolver multipartResolver()
		{
			return new StandardServletMultipartResolver();
		}

		@Bean
		public MultipartConfigElement multipartConfigElement()
		{
			int _256mb = 256 * 1024 * 1024;
			return new MultipartConfigElement(null, _256mb, _256mb, -1);
		}

		@Bean
		public PasswordEncoder passwordEncoder()
		{
			return new BCryptPasswordEncoder();
		}

		@Bean
		public TaskExecutor taskExecutor()
		{
			return new ThreadPoolTaskExecutor();
		}

		@Bean
		public TaskScheduler taskScheduler()
		{
			return new ThreadPoolTaskScheduler();
		}

		@PostConstruct
		public void setup()
		{
			objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		}
	}

	public static void main(String[] args)
	{
		SpringApplication.run(BackendApplication.class, args);
	}
}
