package consulo.review.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * @author VISTALL
 * @since 2025-05-19
 */
@EnableScheduling
@EnableJpaRepositories
@SpringBootApplication
@EnableMethodSecurity(securedEnabled = true)
@EntityScan({
    "consulo.review.backend.*"
})
public class ReviewApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReviewApplication.class, args);
    }
}
