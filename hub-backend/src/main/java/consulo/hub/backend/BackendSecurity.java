package consulo.hub.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import consulo.hub.backend.auth.UserAccountDetailsService;
import consulo.hub.backend.auth.oauth2.*;
import consulo.hub.backend.auth.repository.UserAccountRepository;
import consulo.hub.backend.auth.rsa.RSAKeyJson;
import consulo.hub.shared.auth.HubClaimNames;
import consulo.hub.shared.auth.Roles;
import consulo.hub.shared.auth.domain.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientCredentialsAuthenticationProvider;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.authorization.web.OAuth2TokenEndpointFilter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

	@Autowired
	private ObjectMapper myObjectMapper;

	@Autowired
	private UserAccountAuthorizationRepository myUserAccountAuthorizationRepository;

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
	public JWKSource<SecurityContext> jwkSource() throws Exception
	{
		RSAPublicKey publicKey;
		RSAPrivateKey privateKey;

		Path keyPath = Path.of("jwk_keys.json");
		if(Files.exists(keyPath))
		{
			RSAKeyJson keyJson = myObjectMapper.readValue(keyPath.toFile(), RSAKeyJson.class);

			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			privateKey = (RSAPrivateKey) keyFactory.generatePrivate(keyJson.privateKey.toSpec());
			publicKey = (RSAPublicKey) keyFactory.generatePublic(keyJson.publicKey.toSpec());
		}
		else
		{
			KeyPair keyPair = generateRsaKey();
			publicKey = (RSAPublicKey) keyPair.getPublic();
			privateKey = (RSAPrivateKey) keyPair.getPrivate();
			RSAKeyJson json = new RSAKeyJson(publicKey, privateKey);
			Files.writeString(keyPath, myObjectMapper.writeValueAsString(json));
		}

		RSAKey rsaKey = new RSAKey.Builder(publicKey)
				.privateKey(privateKey)
				.keyID("hub-oauth2-keys")
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
	public OAuth2TokenCustomizer<JwtEncodingContext> jwtEncodingContextOAuth2TokenCustomizer()
	{
		return context ->
		{
			Object o = context.get("org.springframework.security.core.Authentication.AUTHORIZATION_GRANT");
			if(!(o instanceof OAuth2AuthorizationGrantAuthenticationToken))
			{
				return;
			}

			Object details = ((OAuth2AuthorizationGrantAuthenticationToken) o).getDetails();
			if(!(details instanceof OAuth2AuthenticationDetails))
			{
				return;
			}

			context.getClaims().claim(HubClaimNames.CLIENT_NAME, ((OAuth2AuthenticationDetails) details).getClientName());
			context.getClaims().claim(HubClaimNames.SUB_CLIENT_NAME, ((OAuth2AuthenticationDetails) details).getSubClientName());
			context.getClaims().claim(HubClaimNames.REMOTE_ADDRESS, ((OAuth2AuthenticationDetails) details).getRemoteAddress());
		};
	}

	@Bean
	@Order(1)
	public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception
	{
		OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = http.apply(new OAuth2AuthorizationServerConfigurer());

		authorizationServerConfigurer.authorizationEndpoint(Customizer.withDefaults());
		authorizationServerConfigurer.tokenEndpoint(Customizer.withDefaults()).withObjectPostProcessor(new ObjectPostProcessor<OAuth2TokenEndpointFilter>()
		{
			@Override
			public OAuth2TokenEndpointFilter postProcess(OAuth2TokenEndpointFilter object)
			{
				object.setAuthenticationDetailsSource(new OAuth2AuthenticationDetailsSource());
				return object;
			}
		}).withObjectPostProcessor(new ObjectPostProcessor<OAuth2ClientCredentialsAuthenticationProvider>()
		{
			@Override
			public OAuth2ClientCredentialsAuthenticationProvider postProcess(OAuth2ClientCredentialsAuthenticationProvider object)
			{
				return object;
			}
		});

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
			else
			{
				throw new UsernameNotFoundException(principalClaimValue);
			}

			Optional<UserAccountAuthorization> optional = myUserAccountAuthorizationRepository
					.findByStateOrAuthorizationCodeValueOrAccessTokenValueOrRefreshTokenValue(jwt.getTokenValue());
			if(!optional.isPresent())
			{
				throw new AuthenticationCredentialsNotFoundException(jwt.getTokenValue() + " not found");
			}

			UserAccountAuthorization authorization = optional.get();
			if(!Objects.equals(authorization.getPrincipalName(), account.getUsername()))
			{
				throw new BadCredentialsException("Token %s, user %s, expected user %s".formatted(jwt.getTokenValue(), account.getUsername(), authorization.getPrincipalName()));
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
		// anybody can select channel
		http.authorizeHttpRequests().requestMatchers("/api/repository/selectChannel").permitAll();
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
	public OAuth2AuthorizationService oAuth2AuthorizationService(UserAccountAuthorizationRepository userAccountAuthorizationRepository, RegisteredClientRepository registeredClientRepository)
	{
		return new JpaOAuth2AuthorizationService(userAccountAuthorizationRepository, registeredClientRepository);
	}
}
