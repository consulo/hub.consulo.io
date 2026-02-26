package consulo.cloud.www.frontend;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.shared.communication.PushMode;
import com.vaadin.flow.spring.annotation.EnableVaadin;
import com.vaadin.flow.theme.aura.Aura;
import consulo.procoeton.core.ProCore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

/**
 * @author VISTALL
 * @since 2025-05-15
 */
@EnableScheduling
@EnableWebSocket
@SpringBootApplication(exclude = {
    UserDetailsServiceAutoConfiguration.class
})
@EnableVaadin({"consulo.cloud.www.frontend", "consulo.procoeton.core"})
@ComponentScan(basePackageClasses = {ProCore.class, Application.class}, basePackages = "consulo.cloud.www.frontend")
@Push(PushMode.MANUAL)
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.8.14")
@StyleSheet(Aura.STYLESHEET)
@CssImport("./main-layout.css")
@CssImport("./scrollbar.css")
@CssImport("./procoeton-ui.css")
public class Application implements AppShellConfigurator {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
