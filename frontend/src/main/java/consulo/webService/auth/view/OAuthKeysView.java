package consulo.webService.auth.view;

import java.util.Date;
import java.util.List;

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
import com.google.common.base.Strings;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.containers.ContainerUtil;
import com.vaadin.event.ShortcutAction;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import consulo.webService.auth.oauth2.OAuth2ServerConfiguration;
import consulo.webService.auth.oauth2.domain.OAuth2AuthenticationAccessToken;
import consulo.webService.auth.oauth2.mongo.OAuth2AccessTokenRepository;
import consulo.webService.ui.util.TinyComponents;

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
	private final OAuth2AccessTokenRepository myOAuth2AccessTokenRepository;
	private final TaskScheduler myTaskScheduler;

	private VerticalLayout myTokenListPanel;

	@Autowired
	public OAuthKeysView(DefaultTokenServices defaultTokenServices,
			DefaultOAuth2RequestFactory defaultOAuth2RequestFactory,
			OAuth2AccessTokenRepository accessTokenRepository,
			TaskScheduler taskScheduler)
	{
		myTokenServices = defaultTokenServices;
		myOAuth2RequestFactory = defaultOAuth2RequestFactory;
		myOAuth2AccessTokenRepository = accessTokenRepository;
		myTaskScheduler = taskScheduler;
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

		HorizontalLayout header = new HorizontalLayout();
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
				if(myOAuth2AccessTokenRepository.findByUserNameAndName(authentication.getName(), value) != null)
				{
					Notification.show("Duplicate key", Notification.Type.ERROR_MESSAGE);
					return;
				}

				AuthorizationRequest request = new AuthorizationRequest();
				request.setExtensions(ContainerUtil.newHashMap(Pair.create("name", value)));
				request.setScope(ContainerUtil.newArrayList("read"));
				request.setClientId(OAuth2ServerConfiguration.DEFAULT_CLIENT_ID);

				OAuth2Request oAuth2Request = myOAuth2RequestFactory.createOAuth2Request(request);
				OAuth2Authentication oAuth2Authentication = new OAuth2Authentication(oAuth2Request, authentication);
				OAuth2AccessToken accessToken = myTokenServices.createAccessToken(oAuth2Authentication);

				addToken(new OAuth2AuthenticationAccessToken(accessToken, oAuth2Authentication, accessToken.getValue()), false);

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

		myTokenListPanel = new VerticalLayout();
		myTokenListPanel.setSpacing(true);
		myTokenListPanel.addStyleName("bodyMargin");

		addComponent(myTokenListPanel);

		List<OAuth2AuthenticationAccessToken> tokens = myOAuth2AccessTokenRepository.findByClientIdAndUserName(OAuth2ServerConfiguration.DEFAULT_CLIENT_ID, authentication.getName());
		for(OAuth2AuthenticationAccessToken token : tokens)
		{
			addToken(token, true);
		}
	}

	private void addToken(OAuth2AuthenticationAccessToken token, boolean hide)
	{
		HorizontalLayout layout = new HorizontalLayout();
		layout.addStyleName("errorViewLineLayout");
		layout.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
		layout.addStyleName(ValoTheme.LAYOUT_CARD);
		layout.setWidth(100, Unit.PERCENTAGE);

		layout.addComponent(TinyComponents.newLabel("Name: " + token.getName()));
		Label label = TinyComponents.newLabel("Token: " + (hide ? StringUtil.shortenTextWithEllipsis(token.getTokenId(), 18, 7) : token.getTokenId()));
		layout.addComponent(label);
		if(!hide)
		{
			UI current = UI.getCurrent();

			myTaskScheduler.schedule(() ->
			{
				try
				{
					current.access(() -> label.setValue("Token: " + StringUtil.shortenTextWithEllipsis(token.getTokenId(), 18, 7)));
				}
				catch(Exception e)
				{
					// ignored
				}
			}, new Date(System.currentTimeMillis() + 30000L));
		}

		Button revokeButton = new Button("Revoke", e ->
		{
			myTokenServices.revokeToken(token.getTokenId());

			myTokenListPanel.removeComponent(layout);
		});

		revokeButton.addStyleName(ValoTheme.BUTTON_DANGER);
		revokeButton.addStyleName(ValoTheme.BUTTON_TINY);
		layout.addComponent(revokeButton);
		layout.setComponentAlignment(revokeButton, Alignment.MIDDLE_RIGHT);

		myTokenListPanel.addComponent(layout);
	}
}