package consulo.hub.frontend.auth.view;

import com.google.common.base.Strings;
import com.vaadin.event.ShortcutAction;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import consulo.hub.frontend.backend.service.UserAccountService;
import consulo.hub.frontend.base.ui.util.TinyComponents;
import consulo.hub.frontend.base.ui.util.VaadinUIUtil;
import consulo.hub.shared.auth.SecurityUtil;
import consulo.hub.shared.auth.domain.UserAccount;
import consulo.hub.shared.auth.oauth2.domain.OAuthTokenInfo;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author VISTALL
 * @since 27-Sep-16
 */
@SpringView(name = OAuthKeysView.ID)
public class OAuthKeysView extends VerticalLayout implements View
{
	public static final String ID = "oauthKeys";

	private final UserAccountService myUserAccountService;

	private VerticalLayout myTokenListPanel;

	@Autowired
	public OAuthKeysView(UserAccountService userAccountService)
	{
		myUserAccountService = userAccountService;
		setMargin(false);
		setSpacing(false);
		setSizeFull();
	}

	@Override
	public void enter(ViewChangeListener.ViewChangeEvent vce)
	{
		removeAllComponents();

		UserAccount userAccout = SecurityUtil.getUserAccout();
		if(userAccout == null)
		{
			return;
		}

		HorizontalLayout header = VaadinUIUtil.newHorizontalLayout();
		header.setWidth(100, Unit.PERCENTAGE);
		header.addComponent(new Label("OAuth Keys"));
		header.addStyleName("headerMargin");

		Button createKeyButton = TinyComponents.newButton("Add Key", event ->
		{
			Window window = new Window("Enter Name");

			TextField textField = TinyComponents.newTextField();

			textField.addStyleName(ValoTheme.TEXTFIELD_TINY);

			Button okButton = new Button("OK", e ->
			{
				String value = textField.getValue();
				if(Strings.isNullOrEmpty(value) || value.length() >= 255)
				{
					Notification.show("Bad name", Notification.Type.ERROR_MESSAGE);
					return;
				}

				OAuthTokenInfo newToken = myUserAccountService.addOAuthToken(userAccout, value);

				if(newToken != null)
				{
					addToken(newToken, false);
				}

				window.close();
			});
			okButton.setClickShortcut(ShortcutAction.KeyCode.ENTER);
			okButton.addStyleName(ValoTheme.BUTTON_TINY);
			okButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

			HorizontalLayout content = new HorizontalLayout(TinyComponents.newLabel("Name: "), textField, okButton);
			content.setMargin(true);
			content.setSpacing(true);

			window.center();
			window.setContent(content);
			window.setModal(true);
			window.setResizable(false);

			getUI().addWindow(window);

			textField.focus();
		});
		createKeyButton.addStyleName(ValoTheme.BUTTON_FRIENDLY);
		header.addComponent(createKeyButton);
		header.setComponentAlignment(createKeyButton, Alignment.MIDDLE_RIGHT);

		addComponent(header);

		myTokenListPanel = VaadinUIUtil.newVerticalLayout();
		myTokenListPanel.setSpacing(true);
		myTokenListPanel.addStyleName("bodyMargin");

		addComponent(myTokenListPanel);
		setExpandRatio(myTokenListPanel, 1);

		OAuthTokenInfo[] tokens = myUserAccountService.listOAuthTokens(userAccout);
		for(OAuthTokenInfo token : tokens)
		{
			addToken(token, true);
		}
	}

	private void addToken(OAuthTokenInfo token, boolean hide)
	{
		HorizontalLayout layout = VaadinUIUtil.newHorizontalLayout();
		layout.addStyleName("errorViewLineLayout");
		layout.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
		layout.addStyleName(ValoTheme.LAYOUT_CARD);
		layout.setWidth(100, Unit.PERCENTAGE);

		layout.addComponent(TinyComponents.newLabel("Name: " + "??"));
		String tokenId = token.getToken();

		Label label = TinyComponents.newLabel("Token: " + tokenId);
		layout.addComponent(label);

		Button revokeButton = new Button("Revoke", e ->
		{
			UserAccount userAccout = SecurityUtil.getUserAccout();
			if(userAccout == null)
			{
				return;
			}

			if(myUserAccountService.removeOAuthToken(userAccout, token.getToken()) != null)
			{
				myTokenListPanel.removeComponent(layout);
			}
		});

		revokeButton.addStyleName(ValoTheme.BUTTON_DANGER);
		revokeButton.addStyleName(ValoTheme.BUTTON_TINY);
		layout.addComponent(revokeButton);
		layout.setComponentAlignment(revokeButton, Alignment.MIDDLE_RIGHT);

		myTokenListPanel.addComponent(layout);
	}
}