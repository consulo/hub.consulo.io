package consulo.hub.frontend.vflow.user.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import consulo.hub.frontend.vflow.backend.service.BackendUserAccountService;
import consulo.hub.frontend.vflow.base.MainLayout;
import consulo.hub.shared.auth.HubClaimNames;
import consulo.hub.shared.auth.SecurityUtil;
import consulo.hub.shared.auth.domain.UserAccount;
import consulo.hub.shared.auth.oauth2.domain.SessionInfo;
import consulo.procoeton.core.vaadin.ui.ServerOfflineVChildLayout;
import consulo.procoeton.core.vaadin.ui.util.VaadinUIUtil;
import consulo.procoeton.core.vaadin.util.ProcoetonStyles;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.JwtClaimNames;

import java.util.Map;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 2016-09-27
 */
@PageTitle("Sessions")
@Route(value = "user/sessions", layout = MainLayout.class)
@PermitAll
public class UserSessionsView extends ServerOfflineVChildLayout {
    private final BackendUserAccountService myBackendUserAccountService;

    private VerticalLayout myTokenListPanel;

    @Autowired
    public UserSessionsView(BackendUserAccountService backendUserAccountService) {
        super(true);
        myBackendUserAccountService = backendUserAccountService;
    }

    @Override
    protected void buildLayout(Consumer<Component> uiBuilder) {
        UserAccount userAccout = SecurityUtil.getUserAccout();
        if (userAccout == null) {
            return;
        }

        myTokenListPanel = VaadinUIUtil.newVerticalLayout();
        myTokenListPanel.setSpacing(true);

        uiBuilder.accept(myTokenListPanel);

        SessionInfo[] tokens = myBackendUserAccountService.listOAuthTokens(userAccout);
        for (SessionInfo token : tokens) {
            addToken(token);
        }
    }

    private void addToken(SessionInfo token) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        layout.setWidthFull();
        layout.addClassNames(ProcoetonStyles.Border.SOLID, ProcoetonStyles.BorderRadius.MEDIUM, ProcoetonStyles.BorderColor.BASE);

        Map<String, Object> additionalInfo = token.getAdditionalInfo();
        if (additionalInfo == null) {
            additionalInfo = Map.of();
        }

        String issuedAt = (String)additionalInfo.get(JwtClaimNames.IAT);
        String clientName = (String)additionalInfo.get(HubClaimNames.CLIENT_NAME);
        String subClientName = (String)additionalInfo.get(HubClaimNames.SUB_CLIENT_NAME);
        String remoteAddress = (String)additionalInfo.get(HubClaimNames.REMOTE_ADDRESS);

        VerticalLayout infoLayout = new VerticalLayout();
        infoLayout.add(VaadinUIUtil.labeled("Issued at", new Span(issuedAt)));
        infoLayout.add(VaadinUIUtil.labeled("Client", new Span(clientName)));
        infoLayout.add(VaadinUIUtil.labeled("Service", new Span(subClientName)));
        infoLayout.add(VaadinUIUtil.labeled("IP", new Span(remoteAddress)));

        layout.add(infoLayout);

        Button revokeButton = new Button("Close Session", e ->
        {
            UserAccount userAccout = SecurityUtil.getUserAccout();
            if (userAccout == null) {
                return;
            }

            if (myBackendUserAccountService.revokeSessionById(userAccout, token.getId()) != null) {
                myTokenListPanel.remove(layout);
            }
        });

        revokeButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        layout.add(revokeButton);

        myTokenListPanel.add(layout);
    }
}