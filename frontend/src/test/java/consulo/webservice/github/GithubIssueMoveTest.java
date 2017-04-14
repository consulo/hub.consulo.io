package consulo.webservice.github;

import org.junit.Assert;
import org.junit.Test;
import consulo.webService.github.GithubRestController;

/**
 * @author VISTALL
 * @since 14-Apr-17
 */
public class GithubIssueMoveTest extends Assert
{
	@Test
	public void testName()
	{
		assertEquals(GithubRestController.getRepoName("$moveto consulo"), "consulo");
		assertEquals(GithubRestController.getRepoName("$moveto consulo"), "consulo");
		assertEquals(GithubRestController.getRepoName("$moveto consulo.bot.webhook"), "consulo.bot.webhook");
		assertEquals(GithubRestController.getRepoName("thanks\n$moveto consulo"), "consulo");
	}
}
