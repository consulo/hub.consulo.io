package consulo.webService.auth.oauth2.mongo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestFactory;
import org.springframework.security.oauth2.provider.token.AuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.DefaultAuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.TokenStore;
import consulo.webService.auth.oauth2.domain.OAuth2AuthenticationAccessToken;

/**
 * @version 1.0
 * @author: Iain Porter
 * @since 22/05/2013
 * <p>
 * https://github.com/iainporter/oauth2-provider AL2
 */
public class OAuth2RepositoryTokenStore implements TokenStore
{
	private final OAuth2AccessTokenRepository oAuth2AccessTokenRepository;

	private final AuthenticationKeyGenerator authenticationKeyGenerator = new DefaultAuthenticationKeyGenerator();

	private final DefaultOAuth2RequestFactory myOAuth2RequestFactory;

	@Autowired
	public OAuth2RepositoryTokenStore(final OAuth2AccessTokenRepository accessTokenRepository, DefaultOAuth2RequestFactory auth2RequestFactory)
	{
		this.oAuth2AccessTokenRepository = accessTokenRepository;
		myOAuth2RequestFactory = auth2RequestFactory;
	}

	@Override
	public OAuth2Authentication readAuthentication(OAuth2AccessToken token)
	{
		return readAuthentication(token.getValue());
	}

	@Override
	public OAuth2Authentication readAuthentication(String tokenId)
	{
		OAuth2AuthenticationAccessToken token = oAuth2AccessTokenRepository.findByTokenId(tokenId);
		if(token != null)
		{
			AuthorizationRequest request = new AuthorizationRequest();
			request.setClientId(token.getClientId());

			UsernamePasswordAuthenticationToken userAuthentication = new UsernamePasswordAuthenticationToken(token.getUserName(), "N/A", Collections.<GrantedAuthority>emptyList());

			return new OAuth2Authentication(myOAuth2RequestFactory.createOAuth2Request(request), userAuthentication);
		}
		return null;
	}

	@Override
	public void storeAccessToken(OAuth2AccessToken token, OAuth2Authentication authentication)
	{
		OAuth2AuthenticationAccessToken oAuth2AuthenticationAccessToken = new OAuth2AuthenticationAccessToken(token, authentication, authenticationKeyGenerator.extractKey(authentication));
		oAuth2AccessTokenRepository.save(oAuth2AuthenticationAccessToken);
	}

	@Override
	public OAuth2AccessToken readAccessToken(String tokenValue)
	{
		OAuth2AuthenticationAccessToken token = oAuth2AccessTokenRepository.findByTokenId(tokenValue);
		if(token == null)
		{
			return null; //let spring security handle the invalid token
		}
		return token.getoAuth2AccessToken();
	}

	@Override
	public void removeAccessToken(OAuth2AccessToken token)
	{
		OAuth2AuthenticationAccessToken accessToken = oAuth2AccessTokenRepository.findByTokenId(token.getValue());
		if(accessToken != null)
		{
			oAuth2AccessTokenRepository.delete(accessToken);
		}
	}

	@Override
	public void storeRefreshToken(OAuth2RefreshToken refreshToken, OAuth2Authentication authentication)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public OAuth2RefreshToken readRefreshToken(String tokenValue)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public OAuth2Authentication readAuthenticationForRefreshToken(OAuth2RefreshToken token)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeRefreshToken(OAuth2RefreshToken token)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeAccessTokenUsingRefreshToken(OAuth2RefreshToken refreshToken)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public OAuth2AccessToken getAccessToken(OAuth2Authentication authentication)
	{
		String name = (String) authentication.getOAuth2Request().getExtensions().get("name");

		OAuth2AuthenticationAccessToken token = oAuth2AccessTokenRepository.findByAuthenticationId(authenticationKeyGenerator.extractKey(authentication), name);
		return token == null ? null : token.getoAuth2AccessToken();
	}

	@Override
	public Collection<OAuth2AccessToken> findTokensByClientId(String clientId)
	{
		List<OAuth2AuthenticationAccessToken> tokens = oAuth2AccessTokenRepository.findByClientId(clientId);
		return extractAccessTokens(tokens);
	}

	@Override
	public Collection<OAuth2AccessToken> findTokensByClientIdAndUserName(String clientId, String userName)
	{
		List<OAuth2AuthenticationAccessToken> tokens = oAuth2AccessTokenRepository.findByClientIdAndUserName(clientId, userName);
		return extractAccessTokens(tokens);
	}

	private Collection<OAuth2AccessToken> extractAccessTokens(List<OAuth2AuthenticationAccessToken> tokens)
	{
		List<OAuth2AccessToken> accessTokens = new ArrayList<OAuth2AccessToken>();
		for(OAuth2AuthenticationAccessToken token : tokens)
		{
			accessTokens.add(token.getoAuth2AccessToken());
		}
		return accessTokens;
	}

}