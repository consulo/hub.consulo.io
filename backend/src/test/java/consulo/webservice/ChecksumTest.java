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
import java.net.URL;
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
		PluginNode l1 = loadPlugin("consulo.java");
		Thread.sleep(2000L);
		PluginNode l2 = loadPlugin("consulo.java");

		assertArrayEquals(Files.readAllBytes(l1.targetFile.toPath()), Files.readAllBytes(l2.targetFile.toPath()));
		
		assertTrue(MessageDigest.isEqual(Hex.decodeHex(l1.checksum.md5.toCharArray()), Hex.decodeHex(l2.checksum.md5.toCharArray())));
		
		assertTrue(MessageDigest.isEqual(Hex.decodeHex(l1.checksum.sha_256.toCharArray()), Hex.decodeHex(l2.checksum.sha_256.toCharArray())));

		assertTrue(MessageDigest.isEqual(Hex.decodeHex(l1.checksum.sha3_256.toCharArray()), Hex.decodeHex(l2.checksum.sha3_256.toCharArray())));
	}

	private PluginNode loadPlugin(String... pluginIds) throws Exception
	{
		assertTrue(pluginIds.length != 0);

		File tempDir = FileUtil.createTempDirectory("webService", null);

		FileSystemUtils.deleteRecursively(tempDir);

		String canonicalPath = tempDir.getCanonicalPath();

		PluginChannelsService userConfigurationService = new PluginChannelsService(canonicalPath, Runnable::run);

		PluginAnalyzerService pluginAnalyzerService = new PluginAnalyzerService(userConfigurationService);

		PluginDeployService deploy = new PluginDeployService(userConfigurationService, pluginAnalyzerService);

		userConfigurationService.run();

		PluginNode lastNode = null;
		for(String pluginId : pluginIds)
		{
			URL url = new URL("https://api.consulo.io/repository/download?id=" + pluginId + "&platformVersion=SNAPSHOT&version=SNAPSHOT&channel=nightly");

			InputStream resourceAsStream = url.openStream();

			lastNode = deploy.deployPlugin(PluginChannel.alpha, () -> resourceAsStream);
		}

		return lastNode;
	}
}
