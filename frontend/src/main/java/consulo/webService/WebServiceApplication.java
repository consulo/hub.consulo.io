package consulo.webService;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import consulo.webService.auth.VaadinSessionSecurityContextHolderStrategy;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
//@ServletComponentScan(basePackages = "consulo.webService")
//@ComponentScan(basePackages = "consulo.webService")
public class WebServiceApplication extends SpringBootServletInitializer
{
	@Configuration
	public static class Setup
	{
		@Autowired
		private ObjectMapper objectMapper;

		@Bean
		public StandardServletMultipartResolver multipartResolver()
		{
			return new StandardServletMultipartResolver();
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
		@Override
		protected void configure(AuthenticationManagerBuilder auth) throws Exception
		{
			auth.inMemoryAuthentication().withUser("admin").password("p").roles("ADMIN", "USER").and().withUser("user").password("p").roles("USER");
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

	public static void main(String[] args)
	{
		SpringApplication.run(WebServiceApplication.class, args);
	}
}
