package consulo.app.plugins.frontend;

import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.shared.communication.PushMode;
import com.vaadin.flow.spring.annotation.EnableVaadin;
import com.vaadin.flow.theme.Theme;
import consulo.procoeton.core.ProCore;
import jakarta.annotation.Nonnull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

/**
 * @author VISTALL
 * @since 2025-05-10
 */
@EnableCaching
@EnableScheduling
@EnableWebSocket
@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class,
    UserDetailsServiceAutoConfiguration.class
})
@EnableVaadin({"consulo.app.plugins.frontend", "consulo.procoeton.core"})
@ComponentScan(basePackageClasses = {ProCore.class, Application.class}, basePackages = "consulo.app.plugins.frontend")
@Push(PushMode.MANUAL)
@NpmPackage(value = "@fontsource/inter", version = "4.5.0")
@Theme(value = "vflow")
public class Application implements AppShellConfigurator {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void configurePage(@Nonnull AppShellSettings settings) {
        settings.addFavIcon("icon", "/i/consulo.plugin", "16x16");
    }
}
