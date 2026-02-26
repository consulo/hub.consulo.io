package consulo.hub.backend.frontend;

import com.fasterxml.jackson.databind.ObjectMapper;
import consulo.hub.backend.auth.LocalAuthenticationProvider;
import consulo.hub.backend.auth.UserAccountService;
import consulo.hub.backend.auth.oauth2.JpaOAuth2AuthorizationService;
import consulo.hub.backend.auth.oauth2.OAuthKeyRequestService;
import consulo.hub.backend.auth.oauth2.UserAccountAuthorization;
import consulo.hub.backend.auth.oauth2.UserAccountAuthorizationRepository;
import consulo.hub.backend.auth.repository.UserAccountRepository;
import consulo.hub.shared.auth.domain.UserAccount;
import consulo.hub.shared.auth.oauth2.domain.SessionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * @author VISTALL
 * @since 22/08/2021
 */
@RestController
public class FrontendUserRestController {
    private static final Logger LOG = LoggerFactory.getLogger(FrontendUserRestController.class);

    @Autowired
    private LocalAuthenticationProvider myLocalAuthenticationProvider;

    @Autowired
    private UserAccountService myUserAccountService;

    @Autowired
    private PasswordEncoder myPasswordEncoder;

    @Autowired
    private UserAccountAuthorizationRepository myUserAccountAuthorizationRepository;

    @Autowired
    private OAuth2AuthorizationService myOAuth2AuthorizationService;

    @Autowired
    private ObjectMapper myObjectMapper;

    @Autowired
    private UserAccountRepository myUserAccountRepository;

    @Autowired
    private OAuthKeyRequestService myOAuthKeyRequestService;

    @RequestMapping("/api/private/user/register")
    public UserAccount registerUser(@RequestParam("email") String email,
                                    @RequestParam("password") String password,
                                    @AuthenticationPrincipal UserAccount hub) {
        UserAccount userAccount = myUserAccountService.registerUser(email, password);
        return Objects.requireNonNull(userAccount, "null is not allowed");
    }

    @RequestMapping("/api/private/user/list")
    public List<UserAccount> listUsers(@AuthenticationPrincipal UserAccount adminUser) {
        return myUserAccountRepository.findAll();
    }

    @RequestMapping("/api/private/user/oauth/request")
    public Map<String, String> requestKey(@RequestParam("userId") long userId, @RequestParam("token") String token, @RequestParam("hostName") String hostName) {
        myOAuthKeyRequestService.addRequest(userId, token, hostName);
        return Map.of("token", token);
    }

    @RequestMapping("/api/private/user/changePassword")
    public UserAccount userChangePassword(@RequestParam("userId") long userId, @RequestParam("oldPassword") String oldPassword, @RequestParam("newPassword") String newPassword) {
        return myUserAccountService.changePassword(userId, oldPassword, newPassword);
    }

    @RequestMapping("/api/private/user/oauth/list")
    public List<SessionInfo> userOAuthKeys(@AuthenticationPrincipal UserAccount account) {
        List<UserAccountAuthorization> tokens = myUserAccountAuthorizationRepository.findAllByRegisteredClientId(String.valueOf(account.getId()));

        List<SessionInfo> list = new ArrayList<>(tokens.size());
        for (UserAccountAuthorization token : tokens) {
            SessionInfo tokenInfo = mapToInfo(token);
            if (tokenInfo != null) {
                list.add(tokenInfo);
            }
        }

        return list;
    }

    private SessionInfo mapToInfo(UserAccountAuthorization authInfo) {
        OAuth2Authorization authorization = ((JpaOAuth2AuthorizationService) myOAuth2AuthorizationService).toObject(authInfo);

        OAuth2Authorization.Token<OAuth2AccessToken> accessToken = authorization.getAccessToken();
        if (accessToken == null) {
            return null;
        }

        Map<String, Object> claims = accessToken.getClaims();
        return new SessionInfo(authInfo.getId(), claims);
    }

    @RequestMapping("/api/private/user/oauth/revoke/id")
    public SessionInfo userOAuthKeyRevokeId(@RequestParam("tokenId") String tokenId, @AuthenticationPrincipal UserAccount account) {
        Optional<UserAccountAuthorization> optional = myUserAccountAuthorizationRepository.findById(tokenId);
        if (optional.isPresent()) {
            UserAccountAuthorization authorization = optional.get();

            if (!Objects.equals(authorization.getPrincipalName(), account.getUsername())) {
                throw new IllegalArgumentException("Wrong user %s for token %s - expected %s".formatted(authorization.getPrincipalName(), tokenId, account.getUsername()));
            }

            myUserAccountAuthorizationRepository.deleteById(tokenId);

            return mapToInfo(authorization);
        }
        else {
            throw new IllegalArgumentException("Can't find token: %s, userID: %d".formatted(tokenId, account.getId()));
        }
    }

    @RequestMapping("/api/private/user/oauth/revoke/token")
    public SessionInfo userOAuthKeyRevokeToken(@RequestParam("token") String token, @AuthenticationPrincipal UserAccount account) {
        Optional<UserAccountAuthorization> optional = myUserAccountAuthorizationRepository.findByStateOrAuthorizationCodeValueOrAccessTokenValueOrRefreshTokenValue(token);
        if (optional.isPresent()) {
            UserAccountAuthorization authorization = optional.get();

            if (!Objects.equals(authorization.getPrincipalName(), account.getUsername())) {
                throw new IllegalArgumentException("Wrong user %s for token %s - expected %s".formatted(authorization.getPrincipalName(), token, account.getUsername()));
            }

            myUserAccountAuthorizationRepository.delete(authorization);

            return mapToInfo(authorization);
        }
        else {
            throw new IllegalArgumentException("Can't find token: %s, userID: %d".formatted(token, account.getId()));
        }
    }
}
