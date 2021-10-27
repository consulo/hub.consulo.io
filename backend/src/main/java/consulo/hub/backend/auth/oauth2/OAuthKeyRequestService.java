package consulo.hub.backend.auth.oauth2;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import consulo.hub.backend.auth.service.UserAccountService;
import consulo.hub.shared.ServiceClientId;
import consulo.hub.shared.auth.domain.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author VISTALL
 * @since 04/09/2021
 */
@Service
public class OAuthKeyRequestService
{
	private static class TokenInfo
	{
		String hostName;
		long time;
		long userId;

		private TokenInfo(String hostName, long time, long userId)
		{
			this.hostName = hostName;
			this.time = time;
			this.userId = userId;
		}
	}

	@Autowired
	private UserAccountService myUserAccountService;

	@Autowired
	private AuthenticationManager myAuthenticationManager;

	@Autowired
	private TokenStore myTokenStore;

	@Autowired
	private OAuth2RequestFactory myOAuth2RequestFactory;

	@Autowired
	private AuthorizationServerTokenServices myAuthorizationServerTokenServices;

	private Cache<String, TokenInfo> myTokenRequests = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build();

	public void addRequest(long userId, String token, String host)
	{
		myTokenRequests.put(token, new TokenInfo(host, System.currentTimeMillis(), userId));
	}

	@Nullable
	public OAuthRequestResult doRequest(String token)
	{
		TokenInfo tokenInfo = myTokenRequests.asMap().remove(token);
		if(tokenInfo == null)
		{
			return null;
		}

		UserAccount user = myUserAccountService.findUser(tokenInfo.userId);
		if(user == null)
		{
			return null;
		}

		Authentication authenticate = new UsernamePasswordAuthenticationToken(user, "N/A", user.getAuthorities());

		Map<String, String> parameters = Map.of("hostName", tokenInfo.hostName, "time", String.valueOf(tokenInfo.time));
		
		AuthorizationRequest request = new AuthorizationRequest();
		request.setClientId(ServiceClientId.CONSULO_CLIENT_ID);
		request.setExtensions(new HashMap<>(parameters));

		OAuth2Request auth2Request = myOAuth2RequestFactory.createOAuth2Request(request);

		OAuth2Authentication authentication = new OAuth2Authentication(auth2Request, authenticate);

		DefaultOAuth2AccessToken accessToken = (DefaultOAuth2AccessToken) myAuthorizationServerTokenServices.createAccessToken(authentication);

		accessToken.setAdditionalInformation(new LinkedHashMap<>(parameters));

		myTokenStore.storeAccessToken(accessToken, authentication);

		return new OAuthRequestResult(accessToken.getValue(), user);
	}
}
