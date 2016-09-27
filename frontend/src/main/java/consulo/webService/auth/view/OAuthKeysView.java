package consulo.webService.auth.view;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestFactory;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import com.intellij.util.containers.ContainerUtil;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * @author VISTALL
 * @since 27-Sep-16
 */
@SpringView(name = OAuthKeysView.ID)
public class OAuthKeysView extends VerticalLayout implements View
{
	public static final String ID = "oauthKeys";

	@Autowired
	private DefaultTokenServices myTokenServices;

	@Autowired
	private DefaultOAuth2RequestFactory myOAuth2RequestFactory;

	public OAuthKeysView()
	{
		setMargin(true);

		HorizontalLayout header = new HorizontalLayout();
		header.setWidth(100, Unit.PERCENTAGE);
		header.addComponent(new Label("OAuth Keys"));

		Button createKeyButton = new Button("Create Key", event -> {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if(authentication == null)
			{
				return;
			}

			AuthorizationRequest request = new AuthorizationRequest();
			request.setScope(ContainerUtil.newArrayList("read"));

			OAuth2Request oAuth2Request = myOAuth2RequestFactory.createOAuth2Request(request);
			OAuth2AccessToken accessToken = myTokenServices.createAccessToken(new OAuth2Authentication(oAuth2Request, authentication));
			System.out.println("test");
		});
		header.addComponent(createKeyButton);
		header.setComponentAlignment(createKeyButton, Alignment.MIDDLE_RIGHT);

		addComponent(header);
	}

	@Override
	public void enter(ViewChangeListener.ViewChangeEvent event)
	{
	}
}