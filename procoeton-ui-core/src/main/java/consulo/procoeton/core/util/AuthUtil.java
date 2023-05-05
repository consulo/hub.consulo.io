package consulo.procoeton.core.util;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinServletRequest;
import consulo.hub.shared.auth.SecurityUtil;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import java.util.Objects;

/**
 * @author VISTALL
 * @since 21/08/2021
 */
public class AuthUtil
{
	public static long getUserId()
	{
		return Objects.requireNonNull(SecurityUtil.getUserAccout()).getId();
	}

	public static void forceLogout(UI ui)
	{
		SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
		logoutHandler.logout(VaadinServletRequest.getCurrent().getHttpServletRequest(), null, null);
	}
}
