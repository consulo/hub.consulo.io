package consulo.hub.backend.auth.oauth2;

import consulo.hub.backend.auth.oauth2.service.JpaTokenStore;
import consulo.hub.backend.auth.oauth2.service.OAuthAccessTokenRepository;
import consulo.hub.backend.auth.oauth2.service.UserAccountDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestFactory;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;

import javax.sql.DataSource;

/**
 * @author VISTALL
 * @since 26-Sep-16
 */
@Configuration
public class OAuth2ServerConfiguration
{
	public static final String DEFAULT_CLIENT_ID = "consulo";

	private static final String RESOURCE_ID = "restservice";

	@Configuration
	@EnableResourceServer
	protected static class ResourceServerConfiguration extends ResourceServerConfigurerAdapter
	{
		@Override
		public void configure(ResourceServerSecurityConfigurer resources)
		{
			resources.resourceId(RESOURCE_ID);
		}

		@Override
		public void configure(HttpSecurity http) throws Exception
		{
			http.authorizeRequests().antMatchers("/**").authenticated();
		}
	}

	@Configuration
	@EnableAuthorizationServer
	protected static class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter
	{
		@Autowired
		private AuthenticationManager authenticationManager;

		@Autowired
		private UserAccountDetailsService myUserDetailsService;

		@Autowired
		@Lazy
		private ClientDetailsService myClientDetailsService;

		@Autowired
		@Lazy
		private TokenStore myTokenStore;

		@Autowired
		private OAuthAccessTokenRepository myOAuthAccessTokenRepository;

		@Autowired
		@Lazy
		private OAuth2RequestFactory myDefaultOAuth2RequestFactory;

		@Autowired
		private DataSource myDataSource;

		@Override
		public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception
		{
			endpoints.tokenStore(myTokenStore);
			endpoints.authenticationManager(authenticationManager);
			endpoints.userDetailsService(myUserDetailsService);
			endpoints.allowedTokenEndpointRequestMethods(HttpMethod.GET, HttpMethod.POST);
		}

		@Override
		public void configure(ClientDetailsServiceConfigurer clients) throws Exception
		{
			clients.inMemory().withClient(DEFAULT_CLIENT_ID).authorizedGrantTypes("password").authorities("USER").scopes("read", "write").resourceIds(RESOURCE_ID).secret("123456");
		}

		@Bean
		public TokenStore tokenStore()
		{
			return new JpaTokenStore(myOAuthAccessTokenRepository);
		}

		@Bean
		@Primary
		public DefaultTokenServices tokenServices()
		{
			DefaultTokenServices tokenServices = new DefaultTokenServices();
			tokenServices.setSupportRefreshToken(false);
			tokenServices.setAccessTokenValiditySeconds(Integer.MAX_VALUE);
			tokenServices.setTokenStore(myTokenStore);
			return tokenServices;
		}

		@Bean
		public DefaultOAuth2RequestFactory defaultOAuth2RequestFactory()
		{
			return new DefaultOAuth2RequestFactory(myClientDetailsService);
		}
	}
}
