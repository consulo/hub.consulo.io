package consulo.hub.backend.auth.oauth2.service;

import consulo.hub.backend.auth.oauth2.domain.JpaOAuthAccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.common.util.SerializationUtils;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.DefaultAuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.TokenStore;

import javax.transaction.Transactional;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author VISTALL
 * @since 20/08/2021
 *
 * @see org.springframework.security.oauth2.provider.token.store.JdbcTokenStore
 */
@Transactional
public class JpaTokenStore implements TokenStore
{
	private AuthenticationKeyGenerator authenticationKeyGenerator = new DefaultAuthenticationKeyGenerator();

	private OAuthAccessTokenRepository myRepository;

	public JpaTokenStore(OAuthAccessTokenRepository repository)
	{
		myRepository = repository;
	}

	@Override
	public OAuth2Authentication readAuthentication(OAuth2AccessToken token)
	{
		return readAuthentication(token.getValue());
	}

	@Override
	public OAuth2Authentication readAuthentication(String token)
	{
		// select token_id, authentication from oauth_access_token where token_id = ?
		JpaOAuthAccessToken jpa = myRepository.findOne(extractTokenKey(token));
		if(jpa != null)
		{
			try
			{
				return deserializeAuthentication(jpa.getAuthentication());
			}
			catch(Exception e)
			{
				myRepository.delete(jpa);
			}
		}
		return null;
	}

	@Override
	public OAuth2AccessToken readAccessToken(String tokenValue)
	{
		// select token_id, token from oauth_access_token where token_id = ?
		JpaOAuthAccessToken jpa = myRepository.findOne(extractTokenKey(tokenValue));

		Optional<OAuth2AccessToken> optional = convertToToken(jpa);
		if(optional.isPresent())
		{
			return optional.get();
		}
		return null;
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
	public void storeAccessToken(OAuth2AccessToken token, OAuth2Authentication authentication)
	{
		String refreshToken = null;
		if(token.getRefreshToken() != null)
		{
			refreshToken = token.getRefreshToken().getValue();
		}

		if(readAccessToken(token.getValue()) != null)
		{
			removeAccessToken(token.getValue());
		}

		JpaOAuthAccessToken t = new JpaOAuthAccessToken();
		t.setTokenId(extractTokenKey(token.getValue()));
		t.setToken(serializeAccessToken(token));
		t.setAuthenticationId(authenticationKeyGenerator.extractKey(authentication));
		t.setUserName(authentication.isClientOnly() ? null : authentication.getName());
		t.setClientId(authentication.getOAuth2Request().getClientId());
		t.setAuthentication(serializeAuthentication(authentication));
		t.setRefreshToken(extractTokenKey(refreshToken));

		myRepository.save(t);
	}

	protected byte[] serializeAccessToken(OAuth2AccessToken token)
	{
		return SerializationUtils.serialize(token);
	}

	protected byte[] serializeRefreshToken(OAuth2RefreshToken token)
	{
		return SerializationUtils.serialize(token);
	}

	protected byte[] serializeAuthentication(OAuth2Authentication authentication)
	{
		return SerializationUtils.serialize(authentication);
	}

	protected OAuth2AccessToken deserializeAccessToken(byte[] token)
	{
		return SerializationUtils.deserialize(token);
	}

	protected OAuth2RefreshToken deserializeRefreshToken(byte[] token)
	{
		return SerializationUtils.deserialize(token);
	}

	protected OAuth2Authentication deserializeAuthentication(byte[] authentication)
	{
		return SerializationUtils.deserialize(authentication);
	}

	protected String extractTokenKey(String value)
	{
		if(value == null)
		{
			return null;
		}
		MessageDigest digest;
		try
		{
			digest = MessageDigest.getInstance("MD5");
		}
		catch(NoSuchAlgorithmException e)
		{
			throw new IllegalStateException("MD5 algorithm not available.  Fatal (should be in the JDK).");
		}

		try
		{
			byte[] bytes = digest.digest(value.getBytes("UTF-8"));
			return String.format("%032x", new BigInteger(1, bytes));
		}
		catch(UnsupportedEncodingException e)
		{
			throw new IllegalStateException("UTF-8 encoding not available.  Fatal (should be in the JDK).");
		}
	}

	@Override
	public void removeAccessToken(OAuth2AccessToken token)
	{
		removeAccessToken(token.getValue());
	}

	public void removeAccessToken(String tokenValue)
	{
		myRepository.delete(extractTokenKey(tokenValue));
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
		String key = authenticationKeyGenerator.extractKey(authentication);

		// select token_id, token from oauth_access_token where authentication_id = ?

		Optional<OAuth2AccessToken> optional = convertToToken(myRepository.findByAuthenticationId(key));
		if(optional.isPresent())
		{
			return optional.get();
		}

		return null;
	}

	@Override
	public Collection<OAuth2AccessToken> findTokensByClientIdAndUserName(String clientId, String userName)
	{
		List<OAuth2AccessToken> tokens = new ArrayList<>();

		// select token_id, token from oauth_access_token where user_name = ? and client_id = ?
		List<JpaOAuthAccessToken> jpaTokens = myRepository.findAllByUserNameAndClientId(userName, clientId);
		for(JpaOAuthAccessToken jpaToken : jpaTokens)
		{
			convertToToken(jpaToken).ifPresent(tokens::add);
		}
		return tokens;
	}

	@Override
	public Collection<OAuth2AccessToken> findTokensByClientId(String clientId)
	{
		List<OAuth2AccessToken> tokens = new ArrayList<>();

		// select token_id, token from oauth_access_token where client_id = ?
		List<JpaOAuthAccessToken> jpaTokens = myRepository.findAllByClientId(clientId);
		for(JpaOAuthAccessToken jpaToken : jpaTokens)
		{
			convertToToken(jpaToken).ifPresent(tokens::add);
		}
		return tokens;
	}

	private Optional<OAuth2AccessToken> convertToToken(JpaOAuthAccessToken jpaToken)
	{
		if(jpaToken == null)
		{
			return Optional.empty();
		}

		try
		{
			return Optional.ofNullable(deserializeAccessToken(jpaToken.getToken()));
		}
		catch(IllegalArgumentException e)
		{
			myRepository.delete(jpaToken);
			return Optional.empty();
		}
	}
}
