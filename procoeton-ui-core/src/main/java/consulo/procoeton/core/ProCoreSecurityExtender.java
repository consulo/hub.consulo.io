package consulo.procoeton.core;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

/**
 * @author VISTALL
 * @since 2025-05-12
 */
public interface ProCoreSecurityExtender {
    void extend(HttpSecurity httpSecurity) throws Exception;
}
