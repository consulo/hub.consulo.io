package consulo.procoeton.core.vaadin.util;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;

/**
 * @author VISTALL
 * @since 05/05/2023
 */
public class Notifications
{
	public static void info(String message)
	{
		Notification notification = new Notification(message, 3000, Notification.Position.TOP_END);
		notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
		notification.open();
	}

	public static void error(String message)
	{
		Notification notification = new Notification(message, 3000, Notification.Position.TOP_END);
		notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
		notification.open();
	}

	public static void serverOffline()
	{
		error("Server Busy. Try Again Later");
	}
}
