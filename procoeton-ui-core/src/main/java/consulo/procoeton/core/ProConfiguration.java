package consulo.procoeton.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.MultipartConfigElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.websocket.servlet.JettyWebSocketServletWebServerCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

/**
* @author VISTALL
* @since 30/04/2023
*/
@Configuration
public class ProConfiguration
{
	@Autowired
	private ObjectMapper objectMapper;

	private ApplicationContext applicationContext;
	private BeanDefinitionRegistry beanDefinitionRegistry;

	@Bean
	public AuthenticationTrustResolver authenticationTrustResolver()
	{
		return new AuthenticationTrustResolverImpl();
	}

	@Bean
	public JettyWebSocketServletWebServerCustomizer websocketServletWebServerCustomizer()
	{
		return new JettyWebSocketServletWebServerCustomizer();
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
