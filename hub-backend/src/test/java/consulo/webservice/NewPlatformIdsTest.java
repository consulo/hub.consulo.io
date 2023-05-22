package consulo.webservice;

import consulo.hub.shared.repository.util.RepositoryUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author VISTALL
 * @since 22/05/2023
 */
public class NewPlatformIdsTest extends Assert
{
	private static final String[] ourOldPlatformPluginIds = {
			"consulo-win-no-jre",
			"consulo-win",
			"consulo-win64",
			"consulo-winA64",
			"consulo-linux-no-jre",
			"consulo-linux",
			"consulo-linux64",
			"consulo-mac-no-jre",
			"consulo-mac64",
			// special case for windows
			"consulo-win-no-jre" + "-zip",
			"consulo-win" + "-zip",
			"consulo-win64" + "-zip",
			"consulo-winA64" + "-zip",
	};

	@Test
	public void testExtractFileName()
	{
		assertEquals("consulo.dist.linux.no.jre", RepositoryUtil.extractIdFromFileName("consulo.dist.linux.no.jre.tar.gz"));
		assertEquals("consulo.dist.windows.no.jre.zip", RepositoryUtil.extractIdFromFileName("consulo.dist.windows.no.jre.zip.zip"));
		assertEquals("consulo.dist.windows64.installer", RepositoryUtil.extractIdFromFileName("consulo.dist.windows64.installer.exe"));
	}

	@Test
	public void testRemap()
	{
		for(String oldId : ourOldPlatformPluginIds)
		{
			String newPlatformId = RepositoryUtil.mapFromOldPlatformId(oldId);

			assertNotEquals(newPlatformId, oldId);
		}
	}
}
