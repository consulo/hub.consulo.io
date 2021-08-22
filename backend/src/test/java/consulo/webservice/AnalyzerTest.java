package consulo.webservice;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.ArrayUtil;
import consulo.hub.backend.repository.PluginChannelsService;
import consulo.hub.backend.repository.PluginAnalyzerService;
import consulo.hub.backend.repository.PluginDeployService;
import consulo.hub.backend.util.GsonUtil;
import consulo.hub.shared.repository.PluginChannel;
import consulo.hub.shared.repository.PluginNode;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.InputStream;

/**
 * @author VISTALL
 * @since 20-Sep-16
 */
public class AnalyzerTest extends Assert
{
	@Test
	public void testJavaPlugin() throws Exception
	{
		PluginNode pluginNode = loadPlugin("/consulo.java_4615.zip");

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
		PluginNode pluginNode = loadPlugin("/mobi.hsz.idea.gitignore.consulo-plugin");

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
	public void testFileTypeNewExtension() throws Exception
	{
		PluginNode pluginNode = loadPlugin("/net.seesharpsoft.intellij.plugins.csv_1.zip");

		assertEquals(pluginNode.id, "net.seesharpsoft.intellij.plugins.csv");
		assertNotNull(pluginNode.extensionsV2);
		assertNotNull(pluginNode.extensionsV2[0].values[0], "*|csv");
	}

	@Test
	public void testImagesPlugin() throws Exception
	{
		PluginNode pluginNode = loadPlugin("/com.intellij.images_972.zip");

		assertEquals(pluginNode.id, "com.intellij.images");
	}

	@Test
	public void testPluginIcon() throws Exception
	{
		PluginNode pluginNode = loadPlugin("/consulo.nodejs_3624.zip");

		assertEquals(pluginNode.id, "consulo.nodejs");
		assertNotNull(pluginNode.iconBytes);
	}

	private PluginNode loadPlugin(String... pluginPaths) throws Exception
	{
		assertTrue(pluginPaths.length != 0);

		File tempDir = FileUtil.createTempDirectory("webService", null);

		FileSystemUtils.deleteRecursively(tempDir);

		String canonicalPath = tempDir.getCanonicalPath();

		PluginChannelsService userConfigurationService = new PluginChannelsService(canonicalPath, Runnable::run);

		PluginAnalyzerService pluginAnalyzerService = new PluginAnalyzerService(userConfigurationService);

		PluginDeployService deploy = new PluginDeployService(userConfigurationService, pluginAnalyzerService);

		userConfigurationService.run();

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
