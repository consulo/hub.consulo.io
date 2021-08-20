package consulo.hub.frontend.auth.view;

import com.google.common.base.Strings;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.containers.ContainerUtil;
import com.vaadin.event.ShortcutAction;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import consulo.webService.auth.oauth2.OAuth2ServerConfiguration;
import consulo.hub.frontend.base.ui.util.TinyComponents;
import consulo.hub.frontend.base.ui.util.VaadinUIUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestFactory;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;

import java.util.Collection;
import java.util.Date;

/**
 * @author VISTALL
 * @since 27-Sep-16
 */
@SpringView(name = OAuthKeysView.ID)
public class OAuthKeysView extends VerticalLayout implements View
{
	public static final String ID = "oauthKeys";

	private final DefaultTokenServices myTokenServices;
	private final DefaultOAuth2RequestFactory myOAuth2RequestFactory;
	private final TokenStore myTokenStore;
	private final TaskScheduler myTaskScheduler;

	private VerticalLayout myTokenListPanel;

	@Autowired
	public OAuthKeysView(DefaultTokenServices defaultTokenServices,
			DefaultOAuth2RequestFactory defaultOAuth2RequestFactory,
						 TokenStore accessTokenRepository,
			TaskScheduler taskScheduler)
	{
		myTokenServices = defaultTokenServices;
		myOAuth2RequestFactory = defaultOAuth2RequestFactory;
		myTokenStore = accessTokenRepository;
		myTaskScheduler = taskScheduler;

		setMargin(false);
		setSpacing(false);
		setSizeFull();
	}

	@Override
	public void enter(ViewChangeListener.ViewChangeEvent vce)
	{
		removeAllComponents();

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if(authentication == null)
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

//				if(myTokenStore.findByUserNameAndName(authentication.getName(), value) != null)
//				{
//					Notification.show("Duplicate key", Notification.Type.ERROR_MESSAGE);
//					return;
//				}

				AuthorizationRequest request = new AuthorizationRequest();
				request.setExtensions(ContainerUtil.newHashMap(Pair.create("name", value)));
				request.setScope(ContainerUtil.newArrayList("read"));
				request.setClientId(OAuth2ServerConfiguration.DEFAULT_CLIENT_ID);

				OAuth2Request oAuth2Request = myOAuth2RequestFactory.createOAuth2Request(request);
				OAuth2Authentication oAuth2Authentication = new OAuth2Authentication(oAuth2Request, authentication);
				OAuth2AccessToken accessToken = myTokenServices.createAccessToken(oAuth2Authentication);

				addToken(accessToken, false);

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

		Collection<OAuth2AccessToken> tokens = myTokenStore.findTokensByClientIdAndUserName(OAuth2ServerConfiguration.DEFAULT_CLIENT_ID, authentication.getName());
		for(OAuth2AccessToken token : tokens)
		{
			addToken(token, true);
		}
	}

	private void addToken(OAuth2AccessToken token, boolean hide)
	{
		HorizontalLayout layout = VaadinUIUtil.newHorizontalLayout();
		layout.addStyleName("errorViewLineLayout");
		layout.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
		layout.addStyleName(ValoTheme.LAYOUT_CARD);
		layout.setWidth(100, Unit.PERCENTAGE);

		layout.addComponent(TinyComponents.newLabel("Name: " + "??"));
		String tokenId = token.getValue();

		Label label = TinyComponents.newLabel("Token: " + tokenId);
		layout.addComponent(label);
		if(!hide)
		{
			UI current = UI.getCurrent();

			myTaskScheduler.schedule(() ->
			{
				try
				{
					current.access(() -> label.setValue("Token: " + tokenId);
				}
				catch(Exception e)
				{
					// ignored
				}
			}, new Date(System.currentTimeMillis() + 30000L));
		}

		Button revokeButton = new Button("Revoke", e ->
		{
			myTokenServices.revokeToken(tokenId);

			myTokenListPanel.removeComponent(layout);
		});

		revokeButton.addStyleName(ValoTheme.BUTTON_DANGER);
		revokeButton.addStyleName(ValoTheme.BUTTON_TINY);
		layout.addComponent(revokeButton);
		layout.setComponentAlignment(revokeButton, Alignment.MIDDLE_RIGHT);

		myTokenListPanel.addComponent(layout);
	}
}