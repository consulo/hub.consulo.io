//package consulo.hub.backend.configuration;
//
//import consulo.hub.backend.auth.oauth2.OAuthAuthenticationKeyGenerator;
//import consulo.hub.backend.auth.oauth2.service.JpaTokenStore;
//import consulo.hub.backend.auth.oauth2.service.OAuthAccessTokenRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Lazy;
//import org.springframework.context.annotation.Primary;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
//import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
//import org.springframework.security.oauth2.provider.ClientDetailsService;
//import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
//import org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestFactory;
//import org.springframework.security.oauth2.provider.token.AuthenticationKeyGenerator;
//import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
//import org.springframework.security.oauth2.provider.token.TokenStore;
//
///**
// * @author VISTALL
// * @since 21/08/2021
// */
//@Configuration
//@EnableAuthorizationServer
//public class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter
//{
//	@Autowired
//	private AuthenticationManager authenticationManager;
//
//	@Autowired
//	@Lazy
//	private ClientDetailsService myClientDetailsService;
//
//	@Autowired
//	@Lazy
//	private TokenStore myTokenStore;
//
//	@Autowired
//	private OAuthAccessTokenRepository myOAuthAccessTokenRepository;
//
//	@Autowired
//	@Lazy
//	private OAuth2RequestFactory myDefaultOAuth2RequestFactory;
//
//	@Bean
//	public TokenStore tokenStore()
//	{
//		return new JpaTokenStore(myOAuthAccessTokenRepository);
//	}
//
//	@Bean
//	@Primary
//	public DefaultTokenServices tokenServices()
//	{
//		DefaultTokenServices tokenServices = new DefaultTokenServices();
//		tokenServices.setSupportRefreshToken(false);
//		tokenServices.setAccessTokenValiditySeconds(Integer.MAX_VALUE);
//		tokenServices.setTokenStore(myTokenStore);
//		return tokenServices;
//	}
//
//	@Bean
//	public DefaultOAuth2RequestFactory defaultOAuth2RequestFactory()
//	{
//		return new DefaultOAuth2RequestFactory(myClientDetailsService);
//	}
//}
