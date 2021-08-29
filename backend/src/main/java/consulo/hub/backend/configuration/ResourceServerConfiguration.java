package consulo.hub.backend.configuration;

import consulo.hub.backend.auth.oauth2.OAuth2LoginAuthenticationProvider;
import consulo.hub.backend.auth.service.LocalAuthenticationProvider;
import consulo.hub.shared.auth.Roles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;

/**
 * @author VISTALL
 * @since 21/08/2021
 */
@Configuration
@EnableResourceServer
public class ResourceServerConfiguration extends ResourceServerConfigurerAdapter
{
	@Autowired
	private LocalAuthenticationProvider myLocalAuthenticationProvider;

	@Autowired
	private OAuth2LoginAuthenticationProvider myOAuth2LoginAuthenticationProvider;

	@Override
	public void configure(HttpSecurity http) throws Exception
	{
		http.authenticationProvider(myLocalAuthenticationProvider);
		
		http.authenticationProvider(myOAuth2LoginAuthenticationProvider);

		http.authorizeRequests().antMatchers("/api/repository/platformDeploy").hasAuthority(Roles.ROLE_SUPERDEPLOYER);

		http.authorizeRequests().antMatchers("/api/repository/pluginDeploy").hasAuthority(Roles.ROLE_SUPERDEPLOYER);

		// TODO [VISTALL] remove it
		http.authorizeRequests().antMatchers("/api/oauth/auth").permitAll();

		// anybody can create errorReports
		http.authorizeRequests().antMatchers("/api/errorReporter/create").permitAll();
		// statistics can be anonymous
		http.authorizeRequests().antMatchers("/api/statistics/push").permitAll();
		// anybody can list plugins
		http.authorizeRequests().antMatchers("/api/repository/list").permitAll();
		// anybody can download plugins
		http.authorizeRequests().antMatchers("/api/repository/download").permitAll();
		// storage api - only authorized users
		http.authorizeRequests().antMatchers("/api/storage/**").hasAuthority(Roles.ROLE_USER);
		// only user can call validate
		http.authorizeRequests().antMatchers("/api/oauth/validate").hasAuthority(Roles.ROLE_USER);

		// private install can be access without user
		http.authorizeRequests().antMatchers("/api/private/install").permitAll();
		// others require hub right
		http.authorizeRequests().antMatchers("/api/private/**").hasAuthority(Roles.ROLE_HUB);

		http.authorizeRequests().antMatchers("/**").denyAll();
	}
}
