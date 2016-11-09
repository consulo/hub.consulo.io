package consulo.webservice;

import java.io.File;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.util.ArrayUtil;
import consulo.webService.UserConfigurationService;
import consulo.webService.plugins.PluginAnalyzerService;
import consulo.webService.plugins.PluginDeployService;
import consulo.webService.plugins.PluginNode;
import consulo.webService.plugins.PluginChannel;

/**
 * @author VISTALL
 * @since 20-Sep-16
 */
public class AnalyzerTest extends Assert
{
	@Test
	public void testJavaPlugin() throws Exception
	{
		PluginNode pluginNode = loadPlugin("/consulo.java_178.zip");

		assertEquals(pluginNode.extensions.length, 3);
		assertEquals(pluginNode.extensions[0].key, "com.intellij.configurationType");

		assertEquals(pluginNode.extensions[2].values[0], "java");
	}

	@Test
	public void testMavenPlugin() throws Exception
	{
		PluginNode pluginNode = loadPlugin("/com.intellij.xml_108.zip", "/org.jetbrains.idea.maven_149.zip");

		assertEquals(pluginNode.extensions.length, 3);
		assertEquals(pluginNode.extensions[0].key, "com.intellij.configurationType");

		assertTrue(ArrayUtil.contains("*.pom", pluginNode.extensions[1].values));

		assertEquals(pluginNode.extensions[2].values[0], "maven");
	}

	@Test
	public void testXmlPlugin() throws Exception
	{
		PluginNode pluginNode = loadPlugin("/com.intellij.xml_108.zip");

		assertEquals(pluginNode.extensions.length, 1);
		assertEquals(pluginNode.extensions[0].key, "com.intellij.fileTypeFactory");

		assertTrue(ArrayUtil.contains("*.xml", pluginNode.extensions[0].values));

	}

	@Test
	public void testGradlePlugin() throws Exception
	{
		PluginNode pluginNode = loadPlugin("/org.jetbrains.plugins.gradle_155.zip");

		assertEquals(pluginNode.id, "org.jetbrains.plugins.gradle");
		assertNotNull(pluginNode.extensions);
		assertEquals(pluginNode.extensions.length, 1);
		assertEquals(pluginNode.extensions[0].values[0], "GradleRunConfiguration");
	}

	private PluginNode loadPlugin(String... pluginPaths) throws Exception
	{
		assertTrue(pluginPaths.length != 0);

		File tempDir = FileUtil.createTempDirectory("webService", "");
		File[] files = tempDir.listFiles();
		if(files != null)
		{
			for(File child : files)
			{
				FileUtilRt.delete(child);
			}
		}

		String canonicalPath = tempDir.getCanonicalPath();

		UserConfigurationService userConfigurationService = new UserConfigurationService(canonicalPath);

		PluginAnalyzerService pluginAnalyzerService = new PluginAnalyzerService(userConfigurationService);

		PluginDeployService deploy = new PluginDeployService(userConfigurationService, pluginAnalyzerService);

		userConfigurationService.contextInitialized();

		PluginNode lastNode = null;
		for(String pluginPath : pluginPaths)
		{
			InputStream resourceAsStream = AnalyzerTest.class.getResourceAsStream(pluginPath);

			lastNode = deploy.deployPlugin(PluginChannel.internal, () -> resourceAsStream);
		}

		FileUtilRt.delete(tempDir);
		return lastNode;
	}
}
