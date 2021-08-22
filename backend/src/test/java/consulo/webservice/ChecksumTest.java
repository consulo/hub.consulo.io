package consulo.webservice;

import com.intellij.openapi.util.io.FileUtil;
import consulo.hub.backend.repository.PluginAnalyzerService;
import consulo.hub.backend.repository.PluginChannelsService;
import consulo.hub.backend.repository.PluginDeployService;
import consulo.hub.shared.repository.PluginChannel;
import consulo.hub.shared.repository.PluginNode;
import org.apache.commons.codec.binary.Hex;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.MessageDigest;

/**
 * @author VISTALL
 * @since 17/07/2021
 */
public class ChecksumTest extends Assert
{
	@Test
	public void testCheckSum() throws Exception
	{
		PluginNode l1 = loadPlugin("/consulo.java_4615.zip");
		Thread.sleep(2000L);
		PluginNode l2 = loadPlugin("/consulo.java_4615.zip");

		assertArrayEquals(Files.readAllBytes(l1.targetFile.toPath()), Files.readAllBytes(l2.targetFile.toPath()));
		
		assertTrue(MessageDigest.isEqual(Hex.decodeHex(l1.checksum.md5.toCharArray()), Hex.decodeHex(l2.checksum.md5.toCharArray())));
		
		assertTrue(MessageDigest.isEqual(Hex.decodeHex(l1.checksum.sha_256.toCharArray()), Hex.decodeHex(l2.checksum.sha_256.toCharArray())));

		assertTrue(MessageDigest.isEqual(Hex.decodeHex(l1.checksum.sha3_256.toCharArray()), Hex.decodeHex(l2.checksum.sha3_256.toCharArray())));
	}

	private PluginNode loadPlugin(String... pluginPaths) throws Exception
	{
		assertTrue(pluginPaths.length != 0);

		File tempDir = FileUtil.createTempDirectory("webService", null);

		FileSystemUtils.deleteRecursively(tempDir);

		String canonicalPath = tempDir.getCanonicalPath();

		PluginChannelsService userConfigurationService = new PluginChannelsService(canonicalPath);

		PluginAnalyzerService pluginAnalyzerService = new PluginAnalyzerService(userConfigurationService);

		PluginDeployService deploy = new PluginDeployService(userConfigurationService, pluginAnalyzerService);

		userConfigurationService.run();

		PluginNode lastNode = null;
		for(String pluginPath : pluginPaths)
		{
			InputStream resourceAsStream = AnalyzerTest.class.getResourceAsStream(pluginPath);

			lastNode = deploy.deployPlugin(PluginChannel.alpha, () -> resourceAsStream);
		}

		return lastNode;
	}
}
