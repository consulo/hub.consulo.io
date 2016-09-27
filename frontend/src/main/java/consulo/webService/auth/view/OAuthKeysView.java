package consulo.webService.auth.view;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestFactory;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import com.intellij.openapi.util.Pair;
import com.intellij.util.containers.ContainerUtil;
import com.vaadin.data.validator.AbstractStringValidator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import consulo.webService.auth.oauth2.OAuth2ServerConfiguration;
import consulo.webService.auth.oauth2.domain.OAuth2AuthenticationAccessToken;
import consulo.webService.auth.oauth2.mongo.OAuth2AccessTokenRepository;

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

	private VerticalLayout myTokenListPanel;

	@Autowired
	public OAuthKeysView(DefaultTokenServices defaultTokenServices, DefaultOAuth2RequestFactory defaultOAuth2RequestFactory, OAuth2AccessTokenRepository accessTokenRepository)
	{
		myTokenServices = defaultTokenServices;
		myOAuth2RequestFactory = defaultOAuth2RequestFactory;
		myOAuth2AccessTokenRepository = accessTokenRepository;

		setMargin(true);
		setSpacing(true);

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if(authentication == null)
		{
			return;
		}

		HorizontalLayout header = new HorizontalLayout();
		header.setWidth(100, Unit.PERCENTAGE);
		header.addComponent(new Label("OAuth Keys"));

		Button createKeyButton = new Button("Add Key", event -> {
			Window window = new Window("Enter Name");

			TextField textField = new TextField();
			textField.addValidator(new StringLengthValidator("Bad name", 1, 255, false));
			textField.addValidator(new AbstractStringValidator("Duplicate key")
			{
				@Override
				protected boolean isValidValue(String value)
				{
					return myOAuth2AccessTokenRepository.findByUserNameAndName(authentication.getName(), value) == null;
				}
			});
			textField.addStyleName(ValoTheme.TEXTFIELD_TINY);

			Button okButton = new Button("OK", e -> {
				if(!textField.isValid())
				{
					return;
				}

				AuthorizationRequest request = new AuthorizationRequest();
				request.setExtensions(ContainerUtil.newHashMap(Pair.create("name", textField.getValue())));
				request.setScope(ContainerUtil.newArrayList("read"));
				request.setClientId(OAuth2ServerConfiguration.DEFAULT_CLIENT_ID);

				OAuth2Request oAuth2Request = myOAuth2RequestFactory.createOAuth2Request(request);
				OAuth2Authentication oAuth2Authentication = new OAuth2Authentication(oAuth2Request, authentication);
				OAuth2AccessToken accessToken = myTokenServices.createAccessToken(oAuth2Authentication);

				addToken(new OAuth2AuthenticationAccessToken(accessToken, oAuth2Authentication, accessToken.getValue()));

				window.close();
			});
			okButton.addStyleName(ValoTheme.BUTTON_TINY);

			HorizontalLayout content = new HorizontalLayout(new Label("Name: "), textField, okButton);
			content.setMargin(true);
			content.setSpacing(true);

			window.center();
			window.setContent(content);
			window.setModal(true);
			window.setResizable(false);

			getUI().addWindow(window);

			textField.focus();
		});
		header.addComponent(createKeyButton);
		header.setComponentAlignment(createKeyButton, Alignment.MIDDLE_RIGHT);

		addComponent(header);

		myTokenListPanel = new VerticalLayout();
		myTokenListPanel.setSpacing(true);

		addComponent(myTokenListPanel);

		List<OAuth2AuthenticationAccessToken> tokens = myOAuth2AccessTokenRepository.findByClientIdAndUserName(OAuth2ServerConfiguration.DEFAULT_CLIENT_ID, authentication.getName());
		for(OAuth2AuthenticationAccessToken token : tokens)
		{
			addToken(token);
		}
	}

	private void addToken(OAuth2AuthenticationAccessToken token)
	{
		HorizontalLayout layout = new HorizontalLayout();
		layout.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
		layout.addStyleName(ValoTheme.LAYOUT_CARD);
		layout.setWidth(100, Unit.PERCENTAGE);

		layout.addComponent(new Label("Name: " + token.getName()));
		layout.addComponent(new Label("Token: " + hideKey(token.getTokenId())));

		Button revokeButton = new Button("Revoke", e -> {
			myTokenServices.revokeToken(token.getTokenId());

			myTokenListPanel.removeComponent(layout);
		});

		revokeButton.addStyleName(ValoTheme.BUTTON_DANGER);
		revokeButton.addStyleName(ValoTheme.BUTTON_TINY);
		layout.addComponent(revokeButton);
		layout.setComponentAlignment(revokeButton, Alignment.MIDDLE_RIGHT);

		myTokenListPanel.addComponent(layout);
	}

	private static String hideKey(String ori)
	{
		char[] chars = ori.toCharArray();
		for(int i = 0; i < chars.length; i++)
		{
			if(i > 6)
			{
				chars[i] = '*';
			}
		}
		return new String(chars);
	}

	@Override
	public void enter(ViewChangeListener.ViewChangeEvent event)
	{
	}
}