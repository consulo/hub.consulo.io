package consulo.hub.backend;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import consulo.hub.backend.auth.UserAccountDetailsService;
import consulo.hub.backend.auth.repository.UserAccountRepository;
import consulo.hub.shared.auth.Roles;
import consulo.hub.shared.auth.domain.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
		OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = http.apply(new OAuth2AuthorizationServerConfigurer());

		authorizationServerConfigurer.authorizationEndpoint(Customizer.withDefaults());
		authorizationServerConfigurer.tokenEndpoint(Customizer.withDefaults());
		authorizationServerConfigurer.tokenIntrospectionEndpoint(Customizer.withDefaults());
		authorizationServerConfigurer.tokenRevocationEndpoint(Customizer.withDefaults());

		http.httpBasic();

		http.oauth2ResourceServer(it -> it.jwt(jwtConfigurer -> jwtConfigurer.jwtAuthenticationConverter(jwt -> {
			Collection<? extends GrantedAuthority> authorities = List.of();

			String principalClaimValue = jwt.getClaimAsString(JwtClaimNames.SUB);

			UserAccount account = myUserAccountRepository.findByUsername(principalClaimValue);
			if(account != null)
			{
				authorities = account.getAuthorities();
			}
			return new JwtAuthenticationToken(jwt, authorities, principalClaimValue);
		})));

		http.authorizeHttpRequests().requestMatchers("/api/repository/platformDeploy").hasAuthority(Roles.ROLE_SUPERDEPLOYER);

		http.authorizeHttpRequests().requestMatchers("/api/repository/pluginDeploy").hasAuthority(Roles.ROLE_SUPERDEPLOYER);

		// anybody can create errorReports
		http.authorizeHttpRequests().requestMatchers("/api/errorReporter/create").permitAll();
		// only auth user can view self reporters
		http.authorizeHttpRequests().requestMatchers("/api/errorReporter/list").hasAuthority(Roles.ROLE_USER);
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
		// user api - only registered users
		http.authorizeHttpRequests().requestMatchers("/api/user/**").hasAuthority(Roles.ROLE_USER);
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

		// region any user can view statistics of plugins, and direct error view
		http.authorizeHttpRequests().requestMatchers("/api/private/repository/list").permitAll();
		http.authorizeHttpRequests().requestMatchers("/api/private/repository/downloadStat").permitAll();
		http.authorizeHttpRequests().requestMatchers("/api/private/errorReporter/info").permitAll();
		// endregion

		// others require hub right - user must be admin
		http.authorizeHttpRequests().requestMatchers("/api/private/user/register").hasAuthority(Roles.ROLE_HUB);

		// register allowed only to hub
		http.authorizeHttpRequests().requestMatchers("/api/private/**").hasAuthority(Roles.ROLE_SUPERUSER);

		http.authorizeHttpRequests().requestMatchers("/api/oauth2/**").authenticated();

		http.authorizeHttpRequests().requestMatchers("/error").permitAll();

		http.authorizeHttpRequests().requestMatchers("/**").denyAll();

		http.csrf(AbstractHttpConfigurer::disable);

		return http.build();
	}

	@Bean
	public AuthorizationServerSettings authorizationServerSettings()
	{
		AuthorizationServerSettings.Builder builder = AuthorizationServerSettings.builder();
		builder.tokenEndpoint("/api/oauth2/token");
		return builder.build();
	}

	@Bean
	public OAuth2AuthorizationService oAuth2AuthorizationService()
	{
		return new InMemoryOAuth2AuthorizationService();
	}
}
