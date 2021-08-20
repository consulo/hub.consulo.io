package consulo.webservice;

import com.intellij.openapi.util.io.FileUtil;
import consulo.hub.frontend.UserConfigurationService;
import consulo.webService.plugins.PluginAnalyzerService;
import consulo.hub.shared.repository.PluginChannel;
import consulo.webService.plugins.PluginDeployService;
import consulo.hub.shared.repository.PluginNode;
import consulo.hub.frontend.util.PropertyKeys;
import org.apache.commons.codec.binary.Hex;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.Properties;

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

		UserConfigurationService userConfigurationService = new UserConfigurationService(canonicalPath, Runnable::run);
		Properties properties = new Properties();
		properties.setProperty(PropertyKeys.WORKING_DIRECTORY, canonicalPath);

		userConfigurationService.setProperties(properties);

		PluginAnalyzerService pluginAnalyzerService = new PluginAnalyzerService(userConfigurationService);

		PluginDeployService deploy = new PluginDeployService(userConfigurationService, pluginAnalyzerService);

		userConfigurationService.contextInitialized();

		PluginNode lastNode = null;
		for(String pluginPath : pluginPaths)
		{
			InputStream resourceAsStream = AnalyzerTest.class.getResourceAsStream(pluginPath);

			lastNode = deploy.deployPlugin(PluginChannel.alpha, () -> resourceAsStream);
		}

		return lastNode;
	}
}
