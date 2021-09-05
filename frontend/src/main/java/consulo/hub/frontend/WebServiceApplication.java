package consulo.hub.frontend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.spring.navigator.SpringViewProvider;
import consulo.hub.frontend.auth.VaadinSessionSecurityContextHolderStrategy;
import consulo.hub.frontend.backend.BackendAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import javax.annotation.PostConstruct;
import javax.servlet.MultipartConfigElement;

@EnableScheduling
@SpringBootApplication(exclude = {
		DataSourceAutoConfiguration.class,
		SecurityAutoConfiguration.class
})
public class WebServiceApplication extends SpringBootServletInitializer
{
	@Configuration
	public static class Setup
	{
		@Autowired
		private ObjectMapper objectMapper;

		private ApplicationContext applicationContext;
		private BeanDefinitionRegistry beanDefinitionRegistry;

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

	@Configuration
	@EnableGlobalMethodSecurity(securedEnabled = true)
	public static class SecurityConfiguration extends GlobalMethodSecurityConfiguration
	{
		@Autowired
		private BackendAuthenticationProvider myBackendAuthenticationProvider;

		@Override
		protected void configure(AuthenticationManagerBuilder auth) throws Exception
		{
			auth.authenticationProvider(myBackendAuthenticationProvider);
		}

		@Bean
		public AuthenticationManager authenticationManagerBean() throws Exception
		{
			return authenticationManager();
		}

		static
		{
			// Use a custom SecurityContextHolderStrategy
			SecurityContextHolder.setStrategyName(VaadinSessionSecurityContextHolderStrategy.class.getName());
		}
	}

	public WebServiceApplication()
	{
		setRegisterErrorPageFilter(false);
	}

	public static void main(String[] args)
	{
		SpringApplication.run(WebServiceApplication.class, args);
	}
}
