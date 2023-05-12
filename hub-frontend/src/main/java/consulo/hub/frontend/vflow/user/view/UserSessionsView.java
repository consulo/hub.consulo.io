package consulo.hub.frontend.vflow.user.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import consulo.hub.frontend.vflow.backend.service.BackendUserAccountService;
import consulo.hub.frontend.vflow.base.MainLayout;
import consulo.hub.shared.auth.HubClaimNames;
import consulo.hub.shared.auth.SecurityUtil;
import consulo.hub.shared.auth.domain.UserAccount;
import consulo.hub.shared.auth.oauth2.domain.SessionInfo;
import consulo.procoeton.core.vaadin.ui.VChildLayout;
import consulo.procoeton.core.vaadin.ui.util.VaadinUIUtil;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.JwtClaimNames;

import java.util.Map;

/**
 * @author VISTALL
 * @since 27-Sep-16
 */
@PageTitle("User/Sessions")
@Route(value = "user/sessions", layout = MainLayout.class)
@PermitAll
public class UserSessionsView extends VChildLayout
{
	private final BackendUserAccountService myBackendUserAccountService;

	private VerticalLayout myTokenListPanel;

	@Autowired
	public UserSessionsView(BackendUserAccountService backendUserAccountService)
	{
		myBackendUserAccountService = backendUserAccountService;
		setMargin(false);
		setSpacing(false);
		setSizeFull();
	}

	@Override
	public void viewReady(AfterNavigationEvent afterNavigationEvent)
	{
		removeAll();

		UserAccount userAccout = SecurityUtil.getUserAccout();
		if(userAccout == null)
		{
			return;
		}

		myTokenListPanel = VaadinUIUtil.newVerticalLayout();
		myTokenListPanel.setSpacing(true);

		add(myTokenListPanel);
		//setExpandRatio(myTokenListPanel, 1);

		SessionInfo[] tokens = myBackendUserAccountService.listOAuthTokens(userAccout);
		for(SessionInfo token : tokens)
		{
			addToken(token);
		}
	}

	private void addToken(SessionInfo token)
	{
		HorizontalLayout layout = new HorizontalLayout();
		layout.setDefaultVerticalComponentAlignment(Alignment.CENTER);
		layout.setWidthFull();
		layout.addClassNames(LumoUtility.Border.ALL, LumoUtility.BorderRadius.MEDIUM, LumoUtility.BorderColor.CONTRAST_10);

		Map<String, Object> additionalInfo = token.getAdditionalInfo();
		if(additionalInfo == null)
		{
			additionalInfo = Map.of();
		}

		String issuedAt = (String) additionalInfo.get(JwtClaimNames.IAT);
		String clientName = (String) additionalInfo.get(HubClaimNames.CLIENT_NAME);
		String subClientName = (String) additionalInfo.get(HubClaimNames.SUB_CLIENT_NAME);
		String remoteAddress = (String) additionalInfo.get(HubClaimNames.REMOTE_ADDRESS);

		VerticalLayout infoLayout = new VerticalLayout();
		infoLayout.add(VaadinUIUtil.labeled("Issued at", new Label(issuedAt)));
		infoLayout.add(VaadinUIUtil.labeled("Client", new Label(clientName)));
		infoLayout.add(VaadinUIUtil.labeled("Service", new Label(subClientName)));
		infoLayout.add(VaadinUIUtil.labeled("IP", new Label(remoteAddress)));

		layout.add(infoLayout);

		Button revokeButton = new Button("Close Session", e ->
		{
			UserAccount userAccout = SecurityUtil.getUserAccout();
			if(userAccout == null)
			{
				return;
			}

			if(myBackendUserAccountService.revokeSessionById(userAccout, token.getId()) != null)
			{
				myTokenListPanel.remove(layout);
			}
		});

		revokeButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
		layout.add(revokeButton);

		myTokenListPanel.add(layout);
	}
}