package consulo.webservice;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.ArrayUtil;
import consulo.webService.UserConfigurationService;
import consulo.webService.plugins.PluginAnalyzerService;
import consulo.webService.plugins.PluginChannel;
import consulo.webService.plugins.PluginDeployService;
import consulo.webService.plugins.PluginNode;
import consulo.webService.util.GsonUtil;
import consulo.webService.util.PropertyKeys;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;

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

		assertEquals(pluginNode.extensions.length, 4);
		assertEquals(pluginNode.extensions[0].key, "com.intellij.configurationType");

		assertEquals(pluginNode.extensions[2].values[0], "java");
	}

	@Test
	public void testMavenPlugin() throws Exception
	{
		PluginNode pluginNode = loadPlugin("/com.intellij.xml_108.zip", "/org.jetbrains.idea.maven_149.zip");

		assertEquals(pluginNode.extensions.length, 3);
		assertEquals(pluginNode.extensionsV2.length, 3);
		assertEquals(pluginNode.extensions[0].key, "com.intellij.configurationType");
		assertEquals(pluginNode.extensionsV2[0].key, "com.intellij.configurationType");

		assertTrue(ArrayUtil.contains("*.pom", pluginNode.extensions[1].values));
		assertTrue(ArrayUtil.contains("*|pom", pluginNode.extensionsV2[1].values));

		assertEquals(pluginNode.extensions[2].values[0], "maven");
	}

	@Test
	public void testXmlPlugin() throws Exception
	{
		PluginNode pluginNode = loadPlugin("/com.intellij.xml_108.zip");

		assertEquals(pluginNode.extensions.length, 1);
		assertEquals(pluginNode.extensions[0].key, "com.intellij.fileTypeFactory");

		assertTrue(ArrayUtil.contains("*.xml", pluginNode.extensions[0].values));
		assertTrue(ArrayUtil.contains("*|xml", pluginNode.extensionsV2[0].values));
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

	@Test
	public void testDotIgnorePlugin() throws Exception
	{
		PluginNode pluginNode = loadPlugin("/mobi.hsz.idea.gitignore_4.zip");

		assertEquals(pluginNode.id, "mobi.hsz.idea.gitignore");
		assertNotNull(pluginNode.extensions);
		assertEquals(pluginNode.extensions.length, 1);
		assertEquals(pluginNode.extensions[0].values.length, 58);
	}

	@Test
	public void testJavaFxPlugin() throws Exception
	{
		PluginNode pluginNode = loadPlugin("/com.intellij.xml_108.zip", "/org.jetbrains.plugins.javaFX_4413.zip");

		assertEquals(pluginNode.id, "org.jetbrains.plugins.javaFX");

		assertEquals(GsonUtil.prettyGet().toJson(pluginNode), pluginNode.extensionsV2.length, 2);

		assertEquals(GsonUtil.prettyGet().toJson(pluginNode), pluginNode.extensionsV2[0].key, "com.intellij.fileTypeFactory");
		assertEquals(GsonUtil.prettyGet().toJson(pluginNode), pluginNode.extensionsV2[0].values[0], "*|fxml");

		assertEquals(GsonUtil.prettyGet().toJson(pluginNode), pluginNode.extensionsV2[1].key, "com.intellij.packaging.artifactType");
	}

	@Test
	public void testGitPlugin() throws Exception
	{
		PluginNode pluginNode = loadPlugin("/com.intellij.git_583.zip");

		assertEquals(pluginNode.id, "com.intellij.git");
		assertNotNull(pluginNode.extensions);
		assertEquals(pluginNode.extensions.length, 1);
		assertEquals(pluginNode.extensions[0].values[0], "Git");
	}

	@Test
	public void testImagesPlugin() throws Exception
	{
		PluginNode pluginNode = loadPlugin("/com.intellij.images_972.zip");

		assertEquals(pluginNode.id, "com.intellij.images");
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

		FileSystemUtils.deleteRecursively(tempDir);
		return lastNode;
	}
}
