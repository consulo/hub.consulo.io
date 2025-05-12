package consulo.app.plugins.frontend;

import consulo.procoeton.core.ProCoreSecurityExtender;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.stereotype.Component;

/**
 * @author VISTALL
 * @since 2025-05-12
 */
@Component
public class PluginsProCoreSecurityExtender implements ProCoreSecurityExtender {
    @Override
    public void extend(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.authorizeHttpRequests(it -> {
            it.requestMatchers("/i/**").permitAll();
        });
    }
}
