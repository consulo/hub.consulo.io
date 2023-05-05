package consulo.procoeton.core.vaadin.view.login;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.Autocomplete;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility;
import consulo.hub.shared.auth.domain.UserAccount;
import consulo.procoeton.core.auth.backend.BackendAuthenticationToken;
import consulo.procoeton.core.auth.backend.target.BackendAuthTokenTarget;
import consulo.procoeton.core.auth.backend.target.BackendUserInfoTarget;
import consulo.procoeton.core.backend.BackendRequest;
import consulo.procoeton.core.backend.BackendRequestFactory;
import consulo.procoeton.core.vaadin.captcha.Captcha;
import consulo.procoeton.core.vaadin.captcha.CaptchaFactory;
import consulo.procoeton.core.vaadin.util.Notifications;
import consulo.procoeton.core.vaadin.view.CenteredView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * @author VISTALL
 * @since 30/04/2023
 */
@PageTitle("Login")
@Route(value = "login")
@RouteAlias(value = "logout")
@AnonymousAllowed
public class LoginView extends CenteredView implements BeforeEnterObserver
{
	private final CaptchaFactory myCaptchaFactory;
	private final BackendRequestFactory myBackendRequestFactory;

	@Autowired
	public LoginView(CaptchaFactory captchaFactory, BackendRequestFactory backendRequestFactory)
	{
		myCaptchaFactory = captchaFactory;
		myBackendRequestFactory = backendRequestFactory;
	}

	@Override
	protected void fill(VerticalLayout layout, Location location)
	{
		TextField emailField = new TextField("Email");
		emailField.setWidthFull();
		emailField.setAutocomplete(Autocomplete.USERNAME);
		layout.add(emailField);
		PasswordField passwordField = new PasswordField("Password");
		passwordField.setWidthFull();
		passwordField.setAutocomplete(Autocomplete.CURRENT_PASSWORD);
		layout.add(passwordField);

		Captcha captcha = myCaptchaFactory.create();

		layout.add(captcha.getComponent());

		Binder<AuthRequest> binder = new Binder<>();
		binder.forField(emailField)
				.withValidator(AuthValidators.newEmailValidator())
				.asRequired()
				.bind(AuthRequest::getEmail, AuthRequest::setEmail);
		binder.forField(passwordField)
				.withValidator(AuthValidators.newPasswordValidator())
				.asRequired()
				.bind(AuthRequest::getPassword, AuthRequest::setPassword);

		Button loginButton = new Button("Log in", event ->
		{
			try
			{
				AuthRequest request = new AuthRequest();
				binder.writeBean(request);

				if(!captcha.isValid())
				{
					Notifications.error("Captcha failed");
					return;
				}

				BackendRequest<Map<String, Object>> newRequest = myBackendRequestFactory.newRequest(BackendAuthTokenTarget.INSTANCE);
				newRequest.parameter("grant_type", "client_credentials");
				newRequest.authorizationHeader("Basic " + Base64.getEncoder().encodeToString((request.getEmail() + ":" + request.getPassword()).getBytes(StandardCharsets.UTF_8)));

				Map<String, Object> token = newRequest.execute();
				if(token == null)
				{
					throw new BadCredentialsException("");
				}

				String accessToken = (String) token.get("access_token");
				if(accessToken == null)
				{
					throw new BadCredentialsException("");
				}

				BackendRequest<UserAccount> getAccountRequest = myBackendRequestFactory.newRequest(BackendUserInfoTarget.INSTANCE);
				getAccountRequest.authorizationHeader("Bearer " + accessToken);

				UserAccount userAccount = getAccountRequest.execute();

				BackendAuthenticationToken authToken = new BackendAuthenticationToken(userAccount, accessToken, userAccount.getAuthorities());

				SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();

				SecurityContext newContext = securityContextHolderStrategy.createEmptyContext();
				newContext.setAuthentication(authToken);
				securityContextHolderStrategy.setContext(newContext);

				VaadinServletRequest vaadinRequest = VaadinServletRequest.getCurrent();

				if(vaadinRequest != null)
				{
					vaadinRequest.getSession().setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, newContext);

					HttpServletRequest httpRequest = vaadinRequest.getHttpServletRequest();
					if(httpRequest != null)
					{
						HttpSession session = vaadinRequest.getSession(false);
						if(session != null)
						{
							httpRequest.changeSessionId();

							session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
						}

						RequestAttributeSecurityContextRepository repository = new RequestAttributeSecurityContextRepository();

						repository.saveContext(newContext, vaadinRequest, null);
					}
				}

				getUI().ifPresent(ui -> ui.navigate("/"));
			}
			catch(BadCredentialsException e)
			{
				Notifications.error("Invalid user or password");
			}
			catch(ValidationException ignored)
			{
			}
		});
		loginButton.setWidthFull();
		loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		layout.add(loginButton);

		Anchor registeAnchor = new Anchor("/register", "Register");
		registeAnchor.addClassName(LumoUtility.Margin.AUTO);
		layout.add(registeAnchor);
	}

	@Override
	protected String getHeaderText()
	{
		return "Login";
	}

	//	@Override
	//	protected void onAttach(AttachEvent attachEvent)
	//	{
	//		getUI().ifPresent(ui -> ui.getPage().executeJs("return window.matchMedia('(prefers-color-scheme: dark)').matches;").then(Boolean.class, isDark -> {
	//			ui.getElement().getThemeList().add(Lumo.DARK);
	//		}));
	//	}
}
