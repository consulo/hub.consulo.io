package consulo.webservice;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.*;

import javax.annotation.Nonnull;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.FileSystemUtils;
import com.intellij.openapi.util.Couple;
import com.intellij.openapi.util.io.FileUtil;
import consulo.hub.frontend.UserConfigurationService;
import consulo.webService.plugins.PluginAnalyzerService;
import consulo.hub.shared.repository.PluginChannel;
import consulo.webService.plugins.PluginChannelIterationService;
import consulo.webService.plugins.PluginChannelService;
import consulo.webService.plugins.PluginDeployService;
import consulo.hub.shared.repository.PluginNode;
import consulo.webService.plugins.archive.TarGzArchive;
import consulo.webService.plugins.pluginsState.PluginsState;
import consulo.hub.frontend.util.PropertyKeys;

/**
 * @author VISTALL
 * @since 04-Jan-17
 */
public class PluginIterationTest extends Assert
{
	private PluginDeployService myDeployService;
	private PluginChannelIterationService myPluginChannelIterationService;
	private UserConfigurationService myUserConfigurationService;

	private File myTempDir;

	@Before
	public void before() throws Exception
	{
		myTempDir = FileUtil.createTempDirectory("webService", null);

		FileSystemUtils.deleteRecursively(myTempDir);

		String canonicalPath = myTempDir.getCanonicalPath();

		myUserConfigurationService = new UserConfigurationService(canonicalPath, Runnable::run);
		Properties properties = new Properties();
		properties.setProperty(PropertyKeys.WORKING_DIRECTORY, canonicalPath);

		myUserConfigurationService.setProperties(properties);

		PluginAnalyzerService pluginAnalyzerService = new PluginAnalyzerService(myUserConfigurationService);

		myDeployService = new PluginDeployService(myUserConfigurationService, pluginAnalyzerService);

		myPluginChannelIterationService = new PluginChannelIterationService(myUserConfigurationService, myDeployService);

		myUserConfigurationService.contextInitialized();
	}

	@After
	public void after() throws Exception
	{
		FileSystemUtils.deleteRecursively(myTempDir);
	}

	@Test
	public void testCleanupTask() throws Exception
	{
		PluginChannel pluginChannel = PluginChannel.release;
		String platformId = PluginChannelService.ourStandardWinId;

		PluginChannelService channel = myUserConfigurationService.getRepositoryByChannel(pluginChannel);

		int bootBuild = Integer.parseInt(PluginChannelIterationService.ourConsuloBootBuild);
		final int count = 100;
		int start = bootBuild - count;
		int end = bootBuild + count;
		if(start <= 0)
		{
			throw new IllegalArgumentException("bad boot build");
		}

		Set<Integer> platformVersions = new TreeSet<>();
		for(int i = start; i <= end; i++)
		{
			platformVersions.add(i);

			PluginNode pluginNode = new PluginNode();
			pluginNode.version = String.valueOf(i);
			pluginNode.platformVersion = String.valueOf(i);
			pluginNode.id = platformId;

			channel._add(pluginNode);
		}

		Set<Couple<Integer>> toCheckPlatformVersions = new LinkedHashSet<>();
		toCheckPlatformVersions.add(Couple.of(bootBuild, bootBuild));

		for(int i = 0; i < PluginChannelIterationService.ourMaxBuildCount; i++)
		{
			int version = end - i;

			toCheckPlatformVersions.add(Couple.of(version, version));
		}

		String[] dummyPluginIds = new String[]{
				"private",
				"protected",
				"public",
				"internal",
				"local"
		};

		Set<Couple<Integer>> toCheckPluginVersions = new LinkedHashSet<>();

		for(String dummyPluginId : dummyPluginIds)
		{
			for(int i = 0; i <= count; i++)
			{
				for(Integer platformVersion : platformVersions)
				{
					PluginNode pluginNode = new PluginNode();
					pluginNode.version = String.valueOf(i);
					pluginNode.platformVersion = String.valueOf(platformVersion);
					pluginNode.id = dummyPluginId;

					channel._add(pluginNode);
				}
			}
		}

		// generate valid plugin version for each valid platform
		for(Couple<Integer> toCheckPlatformVersion : toCheckPlatformVersions)
		{
			for(int i = 0; i < PluginChannelIterationService.ourMaxBuildCount; i++)
			{
				int version = count - i;

				toCheckPluginVersions.add(Couple.of(toCheckPlatformVersion.getFirst(), version));
			}
		}

		// do it!
		myPluginChannelIterationService.cleanup(pluginChannel);

		Set<String> toCheckIds = new LinkedHashSet<>();
		toCheckIds.add(platformId);
		Collections.addAll(toCheckIds, dummyPluginIds);

		Map<String, PluginsState> states = channel.copyPluginsState();

		// test data after
		for(String toCheckId : toCheckIds)
		{
			// couple with platformVersion + pluginVersion
			boolean platform = false;
			Set<Couple<Integer>> targetVerCheck;
			if(platformId.equals(toCheckId))
			{
				targetVerCheck = toCheckPlatformVersions;
				platform = true;
			}
			else
			{
				targetVerCheck = toCheckPluginVersions;
			}

			PluginsState platforPluginsState = states.get(toCheckId);

			assertNotNull(platforPluginsState);

			NavigableMap<String, NavigableSet<PluginNode>> map = platforPluginsState.getPluginsByPlatformVersion();

			if(platform)
			{
				// max + boot
				assertEquals(PluginChannelIterationService.ourMaxBuildCount + 1, map.size());
			}

			for(Couple<Integer> couple : targetVerCheck)
			{
				NavigableSet<PluginNode> nodes = map.get(String.valueOf(couple.getFirst()));

				// platform contains only one selft build
				assertEquals(platform ? 1 : PluginChannelIterationService.ourMaxBuildCount, nodes.size());

				assertInPluginChannel(nodes, toCheckId, String.valueOf(couple.getFirst()), couple.getSecond());
			}
		}
	}

	private void assertInPluginChannel(Collection<PluginNode> all, String pluginId, String platformVersion, int version)
	{
		Optional<PluginNode> node = all.stream().filter(it -> it.platformVersion.equals(platformVersion) && it.id.equals(pluginId) && it.version.equals(String.valueOf(version))).findFirst();

		assertTrue("Version " + pluginId + ":" + platformVersion + ":" + version + " is not found", node.isPresent());
	}

	@Test
	public void testPlatformIteration() throws Exception
	{
		PluginNode platformNode = deployPlatform(PluginChannel.nightly, 1554, "consulo-win-no-jre", "/consulo-win-no-jre_1554.tar.gz");

		myPluginChannelIterationService.iterate(PluginChannel.nightly, PluginChannel.alpha);

		PluginChannelService pluginChannelService = myUserConfigurationService.getRepositoryByChannel(PluginChannel.alpha);

		PluginNode pluginNodeInAlpha = pluginChannelService.select(platformNode.platformVersion, platformNode.id, null, false);
		assertNotNull(pluginNodeInAlpha);
		assertEquals(pluginNodeInAlpha.id, platformNode.id);
		assertNotNull(pluginNodeInAlpha);
		assertNotNull(pluginNodeInAlpha.targetFile);
		assertTrue(pluginNodeInAlpha.targetFile.exists());

		TarGzArchive archive = new TarGzArchive();

		File testDir = myUserConfigurationService.createTempFile("test_extract_iter", null);

		archive.extract(pluginNodeInAlpha.targetFile, testDir);

		assertTrue(archive.removeEntry("Consulo/.alpha"));
		assertFalse(archive.removeEntry("Consulo/.nightly"));
	}

	@Nonnull
	private PluginNode deployPlatform(PluginChannel channel, int platformVersion, String pluginId, String pluginPath) throws Exception
	{
		InputStream resourceAsStream = AnalyzerTest.class.getResourceAsStream(pluginPath);

		File tempFile = myUserConfigurationService.createTempFile("platformTemp", ".tar.gz");
		try (FileOutputStream outputStream = new FileOutputStream(tempFile))
		{
			FileUtil.copy(resourceAsStream, outputStream);
		}

		return myDeployService.deployPlatform(channel, platformVersion, pluginId, tempFile);
	}

	@Test
	public void testPluginIteration() throws Exception
	{
		PluginNode pluginNode = deployPlugin(PluginChannel.nightly, "/com.intellij.xml_108.zip");

		myPluginChannelIterationService.iterate(PluginChannel.nightly, PluginChannel.alpha);

		PluginChannelService pluginChannelService = myUserConfigurationService.getRepositoryByChannel(PluginChannel.alpha);

		PluginNode pluginNodeInAlpha = pluginChannelService.select(pluginNode.platformVersion, pluginNode.id, null, false);
		assertNotNull(pluginNodeInAlpha);
		assertEquals(pluginNodeInAlpha.id, pluginNode.id);
		assertNotNull(pluginNodeInAlpha);
		assertNotNull(pluginNodeInAlpha.targetFile);
		assertTrue(pluginNodeInAlpha.targetFile.exists());
	}

	private PluginNode deployPlugin(PluginChannel channel, String... pluginPaths) throws Exception
	{
		PluginNode lastNode = null;
		for(String pluginPath : pluginPaths)
		{
			InputStream resourceAsStream = AnalyzerTest.class.getResourceAsStream(pluginPath);

			lastNode = myDeployService.deployPlugin(channel, () -> resourceAsStream);
		}
		return lastNode;
	}
}
