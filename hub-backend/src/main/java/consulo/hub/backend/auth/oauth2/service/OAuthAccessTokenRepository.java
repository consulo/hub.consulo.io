package consulo.hub.backend.auth.oauth2.service;

import consulo.hub.shared.auth.oauth2.domain.JpaOAuthAccessToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author VISTALL
 * @since 20/08/2021
 */
public interface OAuthAccessTokenRepository extends JpaRepository<JpaOAuthAccessToken, String>
{
	List<JpaOAuthAccessToken> findAllByUserNameAndClientId(String userName, String clientId);

	List<JpaOAuthAccessToken> findAllByClientId(String clientId);

	JpaOAuthAccessToken findByAuthenticationId(String authenticationId);
}
