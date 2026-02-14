package consulo.procoeton.core;

import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import consulo.procoeton.core.auth.OAuth2AbstractRememberMeServices;
import consulo.procoeton.core.auth.VaadinAuthenticationManager;
import consulo.procoeton.core.backend.BackendRequestFactory;
import consulo.procoeton.core.vaadin.view.login.LoginView;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.logout.LogoutHandler;

/**
 * @author VISTALL
 * @since 11/05/2023
 */
@Configuration
@EnableWebSecurity
public class ProCoreSecurity {
    @Autowired
    private OAuth2InfoService myOAuth2InfoService;

    @Autowired
    @Lazy
    private ObjectProvider<BackendRequestFactory> myBackendRequestFactory;

    @Autowired
    @Lazy
    private ObjectProvider<ProCoreSecurityExtender> mySecurityExtenders;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.rememberMe(it -> it.rememberMeServices(rememberMeServices()));

        http.authorizeHttpRequests(it -> {
            it.requestMatchers("/line-awesome/**").permitAll();
        });

        for (ProCoreSecurityExtender extender : mySecurityExtenders) {
            extender.extend(http);
        }

        http.with(VaadinSecurityConfigurer.vaadin(), configurer -> {
            configurer.loginView(LoginView.class);
            configurer.enableNavigationAccessControl(false);

            configurer.anyRequest(authorizedUrl -> {
                authorizedUrl.permitAll();
            });
        });

        return http.build();
    }

    @Bean
    public LogoutHandler logoutHandler(OAuth2AbstractRememberMeServices oAuth2AbstractRememberMeServices) {
        return oAuth2AbstractRememberMeServices::logout;
    }

    @Bean
    public RememberMeServices rememberMeServices() {
        OAuth2AbstractRememberMeServices services = new OAuth2AbstractRememberMeServices(myOAuth2InfoService, myBackendRequestFactory);
        services.setAlwaysRemember(true);
        return services;
    }

    @Bean
    public VaadinAuthenticationManager vaadinAuthenticationManager() {
        return new VaadinAuthenticationManager();
    }
}
