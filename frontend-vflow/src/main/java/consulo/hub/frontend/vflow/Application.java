package consulo.hub.frontend.vflow;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.shared.communication.PushMode;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
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
		DataSourceAutoConfiguration.class
})
@Push(PushMode.MANUAL)
@Theme(value = "vflow")
public class Application implements AppShellConfigurator
{
	public static void main(String[] args)
	{
		SpringApplication.run(Application.class, args);
	}
}
