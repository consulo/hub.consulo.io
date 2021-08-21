package consulo.hub.backend.github;

import com.intellij.openapi.util.text.StringUtil;
import consulo.hub.backend.repository.PluginChannelsService;
import org.kohsuke.github.GitHub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * @author VISTALL
 * @since 14-Apr-17
 */
@RestController
public class GithubRestController
{
	// @consulo-bot 21259940
	// @vistall 542934
	private static final int[] ourAdminUserIdss = new int[]{542934};
	private static final int ourBotId = 21259940;

	@Autowired
	private PluginChannelsService myUserConfigurationService;

	@RequestMapping(value = "/github/webhook", method = RequestMethod.POST)
	public ResponseEntity<?> hook(@RequestHeader("X-GitHub-Event") String event, @RequestHeader("X-Hub-Signature") String signature, @RequestBody byte[] array) throws IOException
	{
		String oauthKey = null;//propertySet.getStringProperty(GithubPropertyKeys.OAUTH_KEY);
		if(StringUtil.isEmptyOrSpaces(oauthKey))
		{
			return ResponseEntity.badRequest().build();
		}

		String secretKey = null;//propertySet.getStringProperty(GithubPropertyKeys.SECRET_HOOK_KEY);
		if(!StringUtil.isEmpty(secretKey))
		{
			String bodySignature = generateSha1(array, secretKey);
			if(!bodySignature.equals(signature))
			{
				return ResponseEntity.badRequest().build();
			}
		}

		GitHub gitHub = GitHub.connectUsingOAuth(oauthKey);
		switch(event)
		{
			case "ping":
				return ResponseEntity.ok().build();
			default:
				return ResponseEntity.badRequest().build();
		}
	}

	private static String generateSha1(byte[] data, String secret) throws IOException
	{
		try
		{
			Mac mac = Mac.getInstance("HmacSHA1");
			SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(), mac.getAlgorithm());
			mac.init(keySpec);
			byte[] digest = mac.doFinal(data);

			StringBuilder sb = new StringBuilder("sha1=");
			for(byte b : digest)
			{
				sb.append(String.format("%02x", b));
			}
			return sb.toString();
		}
		catch(NoSuchAlgorithmException | InvalidKeyException e)
		{
			throw new IOException(e);
		}
	}
}
