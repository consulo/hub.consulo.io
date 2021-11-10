package consulo.webservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.ArrayUtil;
import consulo.hub.backend.repository.PluginAnalyzerService;
import consulo.hub.backend.repository.PluginChannelsService;
import consulo.hub.backend.repository.PluginDeployService;
import consulo.hub.backend.util.GsonUtil;
import consulo.hub.shared.repository.PluginChannel;
import consulo.hub.shared.repository.PluginNode;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

/**
 * @author VISTALL
 * @since 20-Sep-16
 */
public class AnalyzerTest extends Assert
{
	@Test
	public void testPermissions() throws Exception
	{
		PluginNode pluginNode = loadPlugin("consulo.dotnet");

		for(PluginNode.Permission permission : pluginNode.permissions)
		{
			if(permission.type.equals("PROCESS_CREATE"))
			{
				return;
			}
		}
		throw new AssertionError("PROCESS_CREATE permission not found");
	}

	@Test
	public void testTags() throws Exception
	{
		PluginNode pluginNode = loadPlugin("consulo.devkit");

		assertEquals(pluginNode.tags.length, 1);
		assertEquals(pluginNode.tags[0], "ide.framework");
	}

	@Test
	public void testJavaPlugin() throws Exception
	{
		PluginNode pluginNode = loadPlugin("consulo.java");

		assertEquals(pluginNode.extensionsV2.length, 4);
		assertEquals(pluginNode.extensionsV2[0].key, "com.intellij.configurationType");
		assertEquals(pluginNode.extensionsV2[2].values[0], "java");
	}

	@Test
	public void testMavenPlugin() throws Exception
	{
		PluginNode pluginNode = loadPlugin("com.intellij.xml", "org.jetbrains.idea.maven");

		assertEquals(pluginNode.extensionsV2.length, 3);
		assertEquals(pluginNode.extensionsV2[0].key, "com.intellij.configurationType");

		assertTrue(ArrayUtil.contains("*|pom", pluginNode.extensionsV2[1].values));

		assertEquals(pluginNode.extensionsV2[2].values[0], "maven");
	}

	@Test
	public void testXmlPlugin() throws Exception
	{
		PluginNode pluginNode = loadPlugin("com.intellij.xml");

		assertEquals(pluginNode.extensionsV2.length, 1);
		assertEquals(pluginNode.extensionsV2[0].key, "com.intellij.fileTypeFactory");

		assertTrue(ArrayUtil.contains("*|xml", pluginNode.extensionsV2[0].values));
	}

	@Test
	public void testGradlePlugin() throws Exception
	{
		PluginNode pluginNode = loadPlugin("org.jetbrains.plugins.gradle");

		assertEquals(pluginNode.id, "org.jetbrains.plugins.gradle");
		assertNotNull(pluginNode.extensionsV2);
		for(PluginNode.Extension extension : pluginNode.extensionsV2)
		{
			if(extension.key.equals("com.intellij.configurationType") && consulo.util.collection.ArrayUtil.contains("GradleRunConfiguration", extension.values))
			{
				return;
			}
		}
		throw new AssertionError("not found run configuration from extension");
	}

	@Test
	public void testDotIgnorePlugin() throws Exception
	{
		PluginNode pluginNode = loadPlugin("mobi.hsz.idea.gitignore");

		assertEquals(pluginNode.id, "mobi.hsz.idea.gitignore");
		assertNotNull(pluginNode.extensionsV2);
		assertEquals(pluginNode.extensionsV2.length, 1);
		assertEquals(pluginNode.extensionsV2[0].values.length, 58);
	}

	@Test
	public void testJavaFxPlugin() throws Exception
	{
		PluginNode pluginNode = loadPlugin("com.intellij.xml", "org.jetbrains.plugins.javaFX");

		assertEquals(pluginNode.id, "org.jetbrains.plugins.javaFX");

		assertEquals(GsonUtil.prettyGet().toJson(pluginNode), pluginNode.extensionsV2.length, 2);

		assertEquals(GsonUtil.prettyGet().toJson(pluginNode), pluginNode.extensionsV2[0].key, "com.intellij.fileTypeFactory");
		assertEquals(GsonUtil.prettyGet().toJson(pluginNode), pluginNode.extensionsV2[0].values[0], "*|fxml");

		assertEquals(GsonUtil.prettyGet().toJson(pluginNode), pluginNode.extensionsV2[1].key, "com.intellij.packaging.artifactType");
	}

	@Test
	public void testGitPlugin() throws Exception
	{
		PluginNode pluginNode = loadPlugin("com.intellij.git");

		assertEquals(pluginNode.id, "com.intellij.git");
		assertNotNull(pluginNode.extensionsV2);
		assertEquals(pluginNode.extensionsV2.length, 1);
		assertEquals(pluginNode.extensionsV2[0].values[0], "Git");
	}

	@Test
	public void testFileTypeNewExtension() throws Exception
	{
		PluginNode pluginNode = loadPlugin("net.seesharpsoft.intellij.plugins.csv");

		assertEquals(pluginNode.id, "net.seesharpsoft.intellij.plugins.csv");
		assertNotNull(pluginNode.extensionsV2);
		assertNotNull(pluginNode.extensionsV2[0].values[0], "*|csv");
	}

	@Test
	public void testImagesPlugin() throws Exception
	{
		PluginNode pluginNode = loadPlugin("com.intellij.images");

		assertEquals(pluginNode.id, "com.intellij.images");
	}

	@Test
	public void testPluginIcon() throws Exception
	{
		PluginNode pluginNode = loadPlugin("consulo.nodejs");

		assertEquals(pluginNode.id, "consulo.nodejs");
		assertNotNull(pluginNode.iconBytes);
	}

	@Test
	public void testDarkIconBytes() throws Exception
	{
		PluginNode pluginNode = loadPlugin("consulo.unity3d");

		assertEquals(pluginNode.id, "consulo.unity3d");
		assertNotNull(pluginNode.iconDarkBytes);
	}

	private PluginNode loadPlugin(String... pluginIds) throws Exception
	{
		assertTrue(pluginIds.length != 0);

		File tempDir = FileUtil.createTempDirectory("webService", null);

		FileSystemUtils.deleteRecursively(tempDir);

		String canonicalPath = tempDir.getCanonicalPath();

		PluginChannelsService userConfigurationService = new PluginChannelsService(canonicalPath, Runnable::run);

		PluginAnalyzerService pluginAnalyzerService = new PluginAnalyzerService(userConfigurationService);

		pluginAnalyzerService.run(new String[0]);

		PluginDeployService deploy = new PluginDeployService(userConfigurationService, pluginAnalyzerService, new ObjectMapper(), new EmptyPluginHistoryServiceImpl());

		userConfigurationService.run();

		PluginNode lastNode = null;
		for(String pluginId : pluginIds)
		{
			URL url = new URL("https://api.consulo.io/repository/download?id=" + pluginId + "&platformVersion=SNAPSHOT&version=SNAPSHOT&channel=nightly");

			InputStream resourceAsStream = url.openStream();

			lastNode = deploy.deployPlugin(PluginChannel.alpha, () -> resourceAsStream);
		}

		FileSystemUtils.deleteRecursively(tempDir);
		return lastNode;
	}
}
