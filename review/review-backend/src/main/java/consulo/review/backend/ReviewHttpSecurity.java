package consulo.review.backend;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * @author VISTALL
 * @since 2025-05-19
 */
@Configuration
@EnableWebSecurity
public class ReviewHttpSecurity {
    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(it -> {
            it.requestMatchers("/api/**").permitAll();
        });

        http.csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }
}
