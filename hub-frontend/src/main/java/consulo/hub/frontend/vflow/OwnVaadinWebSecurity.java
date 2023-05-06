package consulo.hub.frontend.vflow;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import consulo.procoeton.core.vaadin.view.login.LoginView;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

/**
 * @author VISTALL
 * @since 30/04/2023
 */
@Configuration
@EnableWebSecurity
public class OwnVaadinWebSecurity extends VaadinWebSecurity
{
	@Override
	protected void configure(HttpSecurity http) throws Exception
	{
//		http.rememberMe().alwaysRemember(true).tokenRepository(jpaPersistentTokenRepository);
//
//		http
//				.authorizeHttpRequests()
//				.antMatchers("/js/**").permitAll()
//				.antMatchers("/css/**").permitAll()
//				.antMatchers("/images/**").permitAll();

		//http.oauth2Login().loginPage("/login").permitAll();

		super.configure(http);

		setLoginView(http, LoginView.class);
	}
}
