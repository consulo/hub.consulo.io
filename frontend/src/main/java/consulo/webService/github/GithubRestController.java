package consulo.webService.github;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.kohsuke.github.GHEventPayload;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHIssueBuilder;
import org.kohsuke.github.GHIssueComment;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.intellij.openapi.util.text.StringUtil;
import consulo.webService.UserConfigurationService;
import consulo.webService.util.PropertySet;

/**
 * @author VISTALL
 * @since 14-Apr-17
 */
@RestController
public class GithubRestController
{
	private static final Pattern ourPattern = Pattern.compile("\\$moveto (\\w+)");
	private static final String ourMarkdownLine = "\n___\n";

	// @consulo-bot 21259940
	// @vistall 542934
	private static final int[] ourAdminUserIdss = new int[]{542934};
	private static final int ourBotId = 21259940;

	@Autowired
	private UserConfigurationService myUserConfigurationService;

	@RequestMapping(value = "/github/webhook", method = RequestMethod.POST)
	public ResponseEntity<?> hook(@RequestHeader("X-GitHub-Event") String event, @RequestHeader("X-Hub-Signature") String signature, @RequestBody byte[] array) throws IOException
	{
		if(myUserConfigurationService.isNotInstalled())
		{
			return ResponseEntity.badRequest().build();
		}

		PropertySet propertySet = myUserConfigurationService.getPropertySet();

		String oauthKey = propertySet.getStringProperty(GithubPropertyKeys.OAUTH_KEY);
		if(StringUtil.isEmptyOrSpaces(oauthKey))
		{
			return ResponseEntity.badRequest().build();
		}

		String secretKey = propertySet.getStringProperty(GithubPropertyKeys.SECRET_HOOK_KEY);
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
			case "issue_comment":
				return handleIssueCommit(gitHub, array);
			case "ping":
				return ResponseEntity.ok().build();
			default:
				return ResponseEntity.badRequest().build();
		}
	}

	private ResponseEntity<?> handleIssueCommit(GitHub gitHub, byte[] array) throws IOException
	{
		GHEventPayload.IssueComment event = gitHub.parseEventPayload(new InputStreamReader(new ByteArrayInputStream(array), StandardCharsets.UTF_8), GHEventPayload.IssueComment.class);

		if(event.getIssue() == null || event.getIssue().getUser() == null || Arrays.binarySearch(ourAdminUserIdss, event.getComment().getUser().getId()) < 0)
		{
			return ResponseEntity.badRequest().build();
		}

		String body = event.getComment().getBody();
		Matcher matcher = ourPattern.matcher(body);
		if(matcher.find())
		{
			String repositoryName = matcher.group(1);

			GHOrganization consuloOrg = gitHub.getOrganization("consulo");
			GHRepository targetRepository = consuloOrg.getRepository(repositoryName);

			if(targetRepository == null)
			{
				return ResponseEntity.badRequest().build();
			}

			GHRepository thisRepository = gitHub.getRepository(event.getRepository().getFullName());
			GHIssue thisIssue = thisRepository.getIssue(event.getIssue().getNumber());

			String issueText = generateIssueText(thisIssue, event.getComment().getUser().getLogin());

			GHIssueBuilder newIssueBuilder = targetRepository.createIssue(thisIssue.getTitle());
			newIssueBuilder.body(issueText);

			GHIssue newIssue = newIssueBuilder.create();
			List<GHIssueComment> thisComments = thisIssue.getComments();
			for(GHIssueComment comment : thisComments)
			{
				String commentBody = comment.getBody();

				if(ourPattern.matcher(commentBody).find())
				{
					continue;
				}

				String commentText = generateCommentText(commentBody, comment.getUser().getLogin());
				newIssue.comment(commentText);
			}

			thisIssue.comment("This issue moved. New issue " + newIssue.getHtmlUrl());

			thisIssue.close();

			return ResponseEntity.ok().build();
		}
		else
		{
			return ResponseEntity.badRequest().build();
		}
	}

	private static String generateIssueText(GHIssue thisIssue, String commentAuthorLogin)
	{
		String body = thisIssue.getBody();
		body = body.replace("\r\n", "\n"); // remove win ending

		if(thisIssue.getUser().getId() == ourBotId)
		{
			int i = body.indexOf(ourMarkdownLine);
			body = body.substring(i + ourMarkdownLine.length(), body.length());
		}

		StringBuilder builder = new StringBuilder();
		builder.append("Original issue: ");
		builder.append(thisIssue.getHtmlUrl());
		builder.append(". Moved by @");
		builder.append(commentAuthorLogin);
		builder.append(ourMarkdownLine);
		builder.append(body);
		return builder.toString();
	}

	private static String generateCommentText(String body, String commentAuthorLogin)
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Original comment by: @");
		builder.append(commentAuthorLogin);
		builder.append(ourMarkdownLine);
		builder.append(body);
		return builder.toString();
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
