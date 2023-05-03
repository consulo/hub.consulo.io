package consulo.hub.backend;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import consulo.hub.backend.auth.UserAccountDetailsService;
import consulo.hub.backend.auth.repository.UserAccountRepository;
import consulo.hub.shared.auth.Roles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

/**
 * @author VISTALL
 * @since 01/05/2023
 */
@Configuration
@EnableWebSecurity
public class BackendSecurity
{
	@Autowired
	public UserAccountRepository myUserAccountRepository;

	@Bean
	public PasswordEncoder passwordEncoder()
	{
		return new BCryptPasswordEncoder();
	}

	@Bean
	public UserDetailsService userDetailsService()
	{
		return new UserAccountDetailsService(myUserAccountRepository);
	}

	@Bean
	public JWKSource<SecurityContext> jwkSource()
	{
		KeyPair keyPair = generateRsaKey();
		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
		RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
		RSAKey rsaKey = new RSAKey.Builder(publicKey)
				.privateKey(privateKey)
				.keyID(UUID.randomUUID().toString())
				.build();
		JWKSet jwkSet = new JWKSet(rsaKey);
		return new ImmutableJWKSet<>(jwkSet);
	}

	private static KeyPair generateRsaKey()
	{
		KeyPair keyPair;
		try
		{
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			keyPairGenerator.initialize(2048);
			keyPair = keyPairGenerator.generateKeyPair();
		}
		catch(Exception ex)
		{
			throw new IllegalStateException(ex);
		}
		return keyPair;
	}

	@Bean
	public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource)
	{
		return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
	}

	@Bean
	@Order(1)
	public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception
	{

		http.authorizeHttpRequests().requestMatchers("/api/repository/platformDeploy").hasAuthority(Roles.ROLE_SUPERDEPLOYER);

		http.authorizeHttpRequests().requestMatchers("/api/repository/pluginDeploy").hasAuthority(Roles.ROLE_SUPERDEPLOYER);

		// anybody can create errorReports
		http.authorizeHttpRequests().requestMatchers("/api/errorReporter/create").permitAll();
		// statistics can be anonymous
		http.authorizeHttpRequests().requestMatchers("/api/statistics/push").permitAll();
		// anybody can list plugins
		http.authorizeHttpRequests().requestMatchers("/api/repository/list").permitAll();
		// anybody can info about plugin
		http.authorizeHttpRequests().requestMatchers("/api/repository/info").permitAll();
		// anybody can download plugins
		http.authorizeHttpRequests().requestMatchers("/api/repository/download").permitAll();
		// anybody can get history of plugin
		http.authorizeHttpRequests().requestMatchers("/api/repository/history/listByVersion").permitAll();
		http.authorizeHttpRequests().requestMatchers("/api/repository/history/listByVersionRange").permitAll();
		// storage api - only authorized users
		http.authorizeHttpRequests().requestMatchers("/api/storage/**").hasAuthority(Roles.ROLE_USER);
		// only user can call validate
		http.authorizeHttpRequests().requestMatchers("/api/oauth/validate").hasAuthority(Roles.ROLE_USER);
		// anybody can request key by token
		http.authorizeHttpRequests().requestMatchers("/api/oauth/request").permitAll();
		// only developers can get this list
		http.authorizeHttpRequests().requestMatchers("/api/developer/list").hasAuthority(Roles.ROLE_DEVELOPER);

		// private install can be access without user
		http.authorizeHttpRequests().requestMatchers("/api/private/install").permitAll();
		// private test can be access without user
		http.authorizeHttpRequests().requestMatchers("/api/private/test").permitAll();

		// others require hub right
		http.authorizeHttpRequests().requestMatchers("/api/private/**").hasAuthority(Roles.ROLE_HUB);

		http.authorizeHttpRequests().requestMatchers("/**").denyAll();

		http.httpBasic();

		OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
		http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
				.oidc(Customizer.withDefaults());    // Enable OpenID Connect 1.0

		http
				// Redirect to the login page when not authenticated from the
				// authorization endpoint
				.exceptionHandling((exceptions) -> exceptions
						.authenticationEntryPoint(
								new LoginUrlAuthenticationEntryPoint("/login"))
				)
				// Accept access tokens for User Info and/or Client Registration
				.oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt);



		return http.build();
	}

	@Bean
	public AuthorizationServerSettings authorizationServerSettings()
	{
		return AuthorizationServerSettings.builder().build();
	}

	@Bean
	public RegisteredClientRepository registeredClientRepository()
	{
		RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
				.clientId("messaging-client")
				.clientSecret("{noop}secret")
				.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
				.authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
				.authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
				.redirectUri("http://127.0.0.1:22333/login/oauth2/code/messaging-client-oidc")
				.redirectUri("http://127.0.0.1:22333/authorized")
				.scope(OidcScopes.OPENID)
				.scope(OidcScopes.PROFILE)
				.scope(OidcScopes.EMAIL)
				.scope("message.read")
				.scope("message.write")
				.clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
				.build();

		return new InMemoryRegisteredClientRepository(registeredClient);
	}

	//@Bean
	@Order(2)
	public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception
	{
		http.authorizeHttpRequests().requestMatchers("/api/repository/platformDeploy").hasAuthority(Roles.ROLE_SUPERDEPLOYER);

		http.authorizeHttpRequests().requestMatchers("/api/repository/pluginDeploy").hasAuthority(Roles.ROLE_SUPERDEPLOYER);

		// anybody can create errorReports
		http.authorizeHttpRequests().requestMatchers("/api/errorReporter/create").permitAll();
		// statistics can be anonymous
		http.authorizeHttpRequests().requestMatchers("/api/statistics/push").permitAll();
		// anybody can list plugins
		http.authorizeHttpRequests().requestMatchers("/api/repository/list").permitAll();
		// anybody can info about plugin
		http.authorizeHttpRequests().requestMatchers("/api/repository/info").permitAll();
		// anybody can download plugins
		http.authorizeHttpRequests().requestMatchers("/api/repository/download").permitAll();
		// anybody can get history of plugin
		http.authorizeHttpRequests().requestMatchers("/api/repository/history/listByVersion").permitAll();
		http.authorizeHttpRequests().requestMatchers("/api/repository/history/listByVersionRange").permitAll();
		// storage api - only authorized users
		http.authorizeHttpRequests().requestMatchers("/api/storage/**").hasAuthority(Roles.ROLE_USER);
		// only user can call validate
		http.authorizeHttpRequests().requestMatchers("/api/oauth/validate").hasAuthority(Roles.ROLE_USER);
		// anybody can request key by token
		http.authorizeHttpRequests().requestMatchers("/api/oauth/request").permitAll();
		// only developers can get this list
		http.authorizeHttpRequests().requestMatchers("/api/developer/list").hasAuthority(Roles.ROLE_DEVELOPER);

		// private install can be access without user
		http.authorizeHttpRequests().requestMatchers("/api/private/install").permitAll();
		// private test can be access without user
		http.authorizeHttpRequests().requestMatchers("/api/private/test").permitAll();

		// others require hub right
		http.authorizeHttpRequests().requestMatchers("/api/private/**").hasAuthority(Roles.ROLE_HUB);

		http.authorizeHttpRequests().requestMatchers("/**").denyAll();

		http.httpBasic();
		return http.build();
	}
}
