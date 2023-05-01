package consulo.hub.frontend.vflow.auth.view;

import com.google.common.base.Strings;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import consulo.hub.frontend.vflow.backend.service.BackendUserAccountService;
import consulo.hub.frontend.vflow.base.MainLayout;
import consulo.hub.frontend.vflow.base.VChildLayout;
import consulo.hub.frontend.vflow.base.util.TinyComponents;
import consulo.hub.frontend.vflow.base.util.VaadinUIUtil;
import consulo.hub.shared.auth.SecurityUtil;
import consulo.hub.shared.auth.domain.UserAccount;
import consulo.hub.shared.auth.oauth2.domain.OAuthTokenInfo;
import consulo.util.lang.StringUtil;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.Map;

/**
 * @author VISTALL
 * @since 27-Sep-16
 */
@PageTitle("OAuth Keys")
@Route(value = "user/oauth/keys", layout = MainLayout.class)
@PermitAll
public class OAuthKeysView extends VChildLayout
{
	public static final String ID = "oauthKeys";

	private final BackendUserAccountService myBackendUserAccountService;

	private VerticalLayout myTokenListPanel;

	private final Button myCreateButton;

	@Autowired
	public OAuthKeysView(BackendUserAccountService backendUserAccountService)
	{
		myBackendUserAccountService = backendUserAccountService;
		setMargin(false);
		setSpacing(false);
		setSizeFull();

		myCreateButton = TinyComponents.newButton("Add Key", event ->
		{
			Dialog window = new Dialog();
			window.setHeaderTitle("Enter Name");

			TextField textField = TinyComponents.newTextField();

			//textField.addStyleName(ValoTheme.TEXTFIELD_TINY);

			Button okButton = new Button("OK", e ->
			{
				UserAccount userAccout = SecurityUtil.getUserAccout();
				if(userAccout == null)
				{
					return;
				}

				String value = textField.getValue();
				if(Strings.isNullOrEmpty(value) || value.length() >= 255)
				{
					Notification.show("Bad name");
					return;
				}

				OAuthTokenInfo newToken = myBackendUserAccountService.addOAuthToken(userAccout, value);

				if(newToken != null)
				{
					addToken(newToken, false);
				}

				window.close();
			});
			okButton.addClickShortcut(Key.ENTER);
			okButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

			HorizontalLayout content = new HorizontalLayout(TinyComponents.newLabel("Name: "), textField, okButton);
			content.setMargin(true);
			content.setSpacing(true);

			window.add(content);
			window.setModal(true);
			window.setResizable(false);

			window.open();

			textField.focus();
		});
	}

	@Override
	public Component getHeaderRightComponent()
	{
		return myCreateButton;
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

		OAuthTokenInfo[] tokens = myBackendUserAccountService.listOAuthTokens(userAccout);
		for(OAuthTokenInfo token : tokens)
		{
			addToken(token, true);
		}
	}

	private void addToken(OAuthTokenInfo token, boolean hide)
	{
		HorizontalLayout layout = VaadinUIUtil.newHorizontalLayout();
		//		layout.addStyleName("errorViewLineLayout");
		//		layout.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
		//		layout.addStyleName(ValoTheme.LAYOUT_CARD);
		layout.setWidth(100, Unit.PERCENTAGE);

		StringBuilder infoBuilder = new StringBuilder();
		boolean first = true;
		Map<String, Object> additionalInfo = token.getAdditionalInfo();
		if(additionalInfo == null)
		{
			additionalInfo = Map.of();
		}

		for(Map.Entry<String, Object> entry : additionalInfo.entrySet())
		{
			if(first)
			{
				first = false;
			}
			else
			{
				infoBuilder.append(", ");
			}

			infoBuilder.append(StringUtil.capitalize(entry.getKey()));
			infoBuilder.append("=");
			switch(entry.getKey())
			{
				case "time":
					infoBuilder.append(new Date(Long.parseLong((String) entry.getValue())));
					break;
				default:
					infoBuilder.append(entry.getValue());
					break;
			}
		}

		layout.add(TinyComponents.newLabel(infoBuilder.toString()));
		String tokenId = token.getToken();

		Label label = TinyComponents.newLabel("Token: " + tokenId);
		layout.add(label);

		Button revokeButton = new Button("Revoke", e ->
		{
			UserAccount userAccout = SecurityUtil.getUserAccout();
			if(userAccout == null)
			{
				return;
			}

			if(myBackendUserAccountService.removeOAuthToken(userAccout, token.getToken()) != null)
			{
				myTokenListPanel.remove(layout);
			}
		});

		revokeButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
		layout.add(revokeButton);
		//layout.setComponentAlignment(revokeButton, Alignment.MIDDLE_RIGHT);

		myTokenListPanel.add(layout);
	}
}