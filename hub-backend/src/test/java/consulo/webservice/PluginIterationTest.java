package consulo.webservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import consulo.hub.backend.WorkDirectoryService;
import consulo.hub.backend.impl.TempFileServiceImpl;
import consulo.hub.backend.impl.WorkDirectoryServiceImpl;
import consulo.hub.backend.repository.PluginChannelIterationService;
import consulo.hub.backend.repository.impl.store.old.OldPluginChannelService;
import consulo.hub.backend.repository.impl.store.old.OldPluginChannelsService;
import consulo.hub.backend.repository.PluginDeployService;
import consulo.hub.backend.repository.analyzer.PluginAnalyzerServiceImpl;
import consulo.hub.backend.repository.archive.ArchiveData;
import consulo.hub.backend.repository.impl.store.old.OldPluginsState;
import consulo.hub.shared.repository.PluginChannel;
import consulo.hub.shared.repository.PluginNode;
import consulo.hub.shared.repository.util.RepositoryUtil;
import consulo.util.io.FileUtil;
import consulo.util.lang.Couple;
import jakarta.annotation.Nonnull;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * @author VISTALL
 * @since 04-Jan-17
 */
public class PluginIterationTest extends Assert
{
	private PluginDeployService myDeployService;
	private PluginChannelIterationService myPluginChannelIterationService;
	private OldPluginChannelsService myPluginChannelsService;
	private TempFileServiceImpl myFileService;

	private File myTempDir;

	@Before
	public void before() throws Exception
	{
		Path tempDir = Files.createTempDirectory("webService");

		myTempDir = tempDir.toFile();

		FileSystemUtils.deleteRecursively(myTempDir);

		String canonicalPath = myTempDir.getCanonicalPath();

		myFileService = new TempFileServiceImpl(myTempDir);

		WorkDirectoryService workDirectoryService = new WorkDirectoryServiceImpl(canonicalPath);

		myPluginChannelsService = new OldPluginChannelsService(workDirectoryService, myFileService, Runnable::run);

		ObjectMapper objectMapper = new ObjectMapper();

		PluginAnalyzerServiceImpl pluginAnalyzerService = new PluginAnalyzerServiceImpl(myFileService, objectMapper);

		myDeployService = new PluginDeployService(myFileService, pluginAnalyzerService, objectMapper, new EmptyPluginHistoryServiceImpl(), myPluginChannelsService, new EmptyGithubReleaseServiceImpl());

		myPluginChannelIterationService = new PluginChannelIterationService(myPluginChannelsService, myDeployService);

		myPluginChannelsService.init();
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
		String platformId = RepositoryUtil.ourStandardWinId;

		OldPluginChannelService channel = myPluginChannelsService.getRepositoryByChannel(pluginChannel);

		int bootBuild = 1000;
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
		//toCheckPlatformVersions.add(Couple.of(bootBuild, bootBuild));

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

		Map<String, OldPluginsState> states = channel.copyPluginsState();

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

			OldPluginsState platforPluginsState = states.get(toCheckId);

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

		OldPluginChannelService pluginChannelService = myPluginChannelsService.getRepositoryByChannel(PluginChannel.alpha);

		PluginNode pluginNodeInAlpha = pluginChannelService.select(platformNode.platformVersion, platformNode.id, null, false);
		assertNotNull(pluginNodeInAlpha);
		assertEquals(pluginNodeInAlpha.id, platformNode.id);
		assertNotNull(pluginNodeInAlpha);
		assertNotNull(pluginNodeInAlpha.targetFile);
		assertTrue(pluginNodeInAlpha.targetFile.exists());

		ArchiveData archive = new ArchiveData();

		File testDir = myFileService.createTempFile("test_extract_iter", null);

		archive.extract(pluginNodeInAlpha.targetFile, testDir);

		assertTrue(archive.removeEntry("Consulo/.alpha"));
		assertFalse(archive.removeEntry("Consulo/.nightly"));
	}

	@Nonnull
	private PluginNode deployPlatform(PluginChannel channel, int platformVersion, String pluginId, String pluginPath) throws Exception
	{
		InputStream resourceAsStream = AnalyzerTest.class.getResourceAsStream(pluginPath);

		File tempFile = myFileService.createTempFile("platformTemp", ".tar.gz");
		try (FileOutputStream outputStream = new FileOutputStream(tempFile))
		{
			FileUtil.copy(resourceAsStream, outputStream);
		}

		return myDeployService.deployPlatform(channel, null, platformVersion, pluginId, tempFile.toPath());
	}

	@Test
	public void testPluginIteration() throws Exception
	{
		PluginNode pluginNode = deployPlugin(PluginChannel.nightly, "com.intellij.xml");

		myPluginChannelIterationService.iterate(PluginChannel.nightly, PluginChannel.alpha);

		OldPluginChannelService pluginChannelService = myPluginChannelsService.getRepositoryByChannel(PluginChannel.alpha);

		PluginNode pluginNodeInAlpha = pluginChannelService.select(pluginNode.platformVersion, pluginNode.id, null, false);
		assertNotNull(pluginNodeInAlpha);
		assertEquals(pluginNodeInAlpha.id, pluginNode.id);
		assertNotNull(pluginNodeInAlpha);
		assertNotNull(pluginNodeInAlpha.targetFile);
		assertTrue(pluginNodeInAlpha.targetFile.exists());
	}

	private PluginNode deployPlugin(PluginChannel channel, String... pluginIds) throws Exception
	{
		PluginNode lastNode = null;
		for(String pluginId : pluginIds)
		{
			URL url = new URL("https://api.consulo.io/repository/download?id=" + pluginId + "&platformVersion=SNAPSHOT&version=SNAPSHOT&channel=nightly");

			InputStream resourceAsStream = url.openStream();

			lastNode = myDeployService.deployPlugin(channel, () -> resourceAsStream);
		}
		return lastNode;
	}
}
