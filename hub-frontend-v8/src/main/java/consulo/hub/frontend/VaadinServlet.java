package consulo.hub.frontend;

import com.vaadin.server.CustomizedSystemMessages;
import com.vaadin.server.SystemMessagesProvider;
import com.vaadin.spring.server.SpringVaadinServlet;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;

@Component("vaadinServlet")
public class VaadinServlet extends SpringVaadinServlet
{
	@Override
	protected void servletInitialized() throws ServletException
	{
		super.servletInitialized();
		getService().setSystemMessagesProvider((SystemMessagesProvider) systemMessagesInfo -> {
			CustomizedSystemMessages messages = new CustomizedSystemMessages();
			// Don't show any messages, redirect immediately to the session expired URL
			messages.setSessionExpiredNotificationEnabled(false);
			// Don't show any message, reload the page instead
			messages.setCommunicationErrorNotificationEnabled(false);
			return messages;
		});
	}
}
