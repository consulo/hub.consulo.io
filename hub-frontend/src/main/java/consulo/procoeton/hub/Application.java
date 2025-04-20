package consulo.procoeton.hub;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.shared.communication.PushMode;
import com.vaadin.flow.spring.annotation.EnableVaadin;
import com.vaadin.flow.theme.Theme;
import consulo.procoeton.core.ProCore;
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
 * @since 30/04/2023
 */
@EnableCaching
@EnableScheduling
@EnableWebSocket
@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class,
    UserDetailsServiceAutoConfiguration.class
})
@EnableVaadin({"consulo.hub.frontend.vflow", "consulo.procoeton.core", "consulo.procoeton.hub"})
@ComponentScan(basePackageClasses = {ProCore.class, Application.class}, basePackages = "consulo.hub.frontend.vflow")
@Push(PushMode.MANUAL)
@Theme(value = "vflow")
@Uses(AppLayout.class)
public class Application implements AppShellConfigurator {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
