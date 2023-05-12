package consulo.procoeton.core.service;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinServletResponse;
import consulo.hub.shared.auth.domain.UserAccount;
import consulo.procoeton.core.auth.backend.BackendAuthenticationToken;
import consulo.procoeton.core.auth.backend.BackendUserAccountServiceCore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;

/**
 * @author VISTALL
 * @since 11/05/2023
 */
@Service
public class LogoutService
{
	private ObjectProvider<BackendUserAccountServiceCore> myBackendUserAccountServiceCore;
	private RememberMeServices myRememberMeServices;

	@Autowired
	public LogoutService(ObjectProvider<BackendUserAccountServiceCore> backendUserAccountServiceCore, RememberMeServices oAuth2AbstractRememberMeServices)
	{
		myBackendUserAccountServiceCore = backendUserAccountServiceCore;
		myRememberMeServices = oAuth2AbstractRememberMeServices;
	}

	public void logout(UI ui, boolean revokeSessionKey)
	{
		SecurityContext securityContext = SecurityContextHolder.getContext();
		Authentication authentication = securityContext.getAuthentication();
		if(!(authentication instanceof BackendAuthenticationToken backendAuthenticationToken))
		{
			return;
		}

		String token = backendAuthenticationToken.getToken();
		UserAccount userAccount = backendAuthenticationToken.getPrincipal();

		if(revokeSessionKey)
		{
			myBackendUserAccountServiceCore.getObject().revokeSessionByKey(userAccount, token);
		}

		SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
		VaadinServletRequest request = VaadinServletRequest.getCurrent();
		if(request != null)
		{
			logoutHandler.logout(request.getHttpServletRequest(), null, null);

			VaadinServletResponse response = VaadinServletResponse.getCurrent();
			if(response != null)
			{
				((LogoutHandler) myRememberMeServices).logout(request, response, authentication);
			}
		}
		else
		{
			SecurityContextHolder.clearContext();
		}
	}
}
