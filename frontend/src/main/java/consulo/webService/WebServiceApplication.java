package consulo.webService;

import javax.annotation.PostConstruct;
import javax.servlet.MultipartConfigElement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import consulo.webService.auth.Roles;
import consulo.webService.auth.VaadinSessionSecurityContextHolderStrategy;
import consulo.webService.auth.mongo.domain.Role;
import consulo.webService.auth.mongo.service.LocalAuthenticationProvider;

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

		@Bean
		public MultipartConfigElement multipartConfigElement()
		{
			int _128mb = 128 * 1024 * 1024;
			return new MultipartConfigElement(null, _128mb, _128mb, -1);
		}

		@Bean
		public PasswordEncoder passwordEncoder()
		{
			return new BCryptPasswordEncoder();
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
		private LocalAuthenticationProvider myLocalAuthenticationProvider;

		@Autowired
		private MongoOperations myMongoOperations;

		//@Autowired
		//protected DbService dbService;

		@Override
		protected void configure(AuthenticationManagerBuilder auth) throws Exception
		{
			auth.authenticationProvider(myLocalAuthenticationProvider);

			try
			{
				myMongoOperations.insert(new Role(Roles.ROLE_USER), "role");
				myMongoOperations.insert(new Role(Roles.ROLE_ADMIN), "role");
			}
			catch(DuplicateKeyException e)
			{
				// don't throw error on duplicate
			}
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
