package consulo.webservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import consulo.hub.backend.impl.AsyncTempFileServiceImpl;
import consulo.hub.backend.impl.WorkDirectoryServiceImpl;
import consulo.hub.backend.repository.PluginDeployService;
import consulo.hub.backend.repository.RepositoryChannelIterationService;
import consulo.hub.backend.repository.RepositoryChannelStore;
import consulo.hub.backend.repository.RepositoryChannelsService;
import consulo.hub.backend.repository.analyzer.PluginAnalyzerServiceImpl;
import consulo.hub.backend.repository.analyzer.builtin.BuiltinPluginAnalyzerRunnerFactory;
import consulo.hub.backend.repository.impl.store.neww.NewRepositoryChannelIterationService;
import consulo.hub.backend.repository.impl.store.neww.NewRepositoryChannelsService;
import consulo.hub.shared.repository.PluginChannel;
import consulo.hub.shared.repository.PluginNode;
import consulo.hub.shared.repository.util.RepositoryUtil;
import consulo.util.io.FileUtil;
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
import java.util.Collection;
import java.util.Optional;

/**
 * @author VISTALL
 * @since 04-Jan-17
 */
public class PluginIterationTest extends Assert {
    private PluginDeployService myDeployService;
    private RepositoryChannelIterationService myPluginChannelIterationService;
    private RepositoryChannelsService myPluginChannelsService;
    private AsyncTempFileServiceImpl myFileService;

    private Path myTempDir;

    @Before
    public void before() throws Exception {
        Path tempDir = Files.createTempDirectory("webService");

        myTempDir = tempDir;

        Files.createDirectories(myTempDir);

        WorkDirectoryServiceImpl workDirectoryService = new WorkDirectoryServiceImpl(myTempDir.toAbsolutePath().toString());
        workDirectoryService.init();

        myFileService = new SyncTempFileServiceImpl(workDirectoryService);
        myFileService.init();

        myPluginChannelsService = new NewRepositoryChannelsService(workDirectoryService, myFileService, Runnable::run);
        myPluginChannelsService.init();

        ObjectMapper objectMapper = new ObjectMapper();

        PluginAnalyzerServiceImpl pluginAnalyzerService = new PluginAnalyzerServiceImpl(myFileService, new BuiltinPluginAnalyzerRunnerFactory(objectMapper));

        myDeployService = new PluginDeployService(myFileService, pluginAnalyzerService, objectMapper, new EmptyPluginHistoryServiceImpl(), myPluginChannelsService, new EmptyGithubReleaseServiceImpl());

        myPluginChannelIterationService = new NewRepositoryChannelIterationService((NewRepositoryChannelsService) myPluginChannelsService);

        myPluginChannelsService.init();
    }

    @After
    public void after() throws Exception {
        FileSystemUtils.deleteRecursively(myTempDir);
    }

//	@Test
//	public void testCleanupTask() throws Exception
//	{
//		PluginChannel pluginChannel = PluginChannel.release;
//		String platformId = RepositoryUtil.ourStandardWinId;
//
//		NewRepositoryChannelStore channel = (NewRepositoryChannelStore) myPluginChannelsService.getRepositoryByChannel(pluginChannel);
//
//		int bootBuild = 1000;
//		final int count = 100;
//		int start = bootBuild - count;
//		int end = bootBuild + count;
//		if(start <= 0)
//		{
//			throw new IllegalArgumentException("bad boot build");
//		}
//
//		Set<Integer> platformVersions = new TreeSet<>();
//		for(int i = start; i <= end; i++)
//		{
//			platformVersions.add(i);
//
//			PluginNode pluginNode = new PluginNode();
//			pluginNode.version = String.valueOf(i);
//			pluginNode.platformVersion = String.valueOf(i);
//			pluginNode.id = platformId;
//
//			channel._add(pluginNode);
//		}
//
//		Set<Couple<Integer>> toCheckPlatformVersions = new LinkedHashSet<>();
//		//toCheckPlatformVersions.add(Couple.of(bootBuild, bootBuild));
//
//		for(int i = 0; i < PluginChannelIterationService.ourMaxBuildCount; i++)
//		{
//			int version = end - i;
//
//			toCheckPlatformVersions.add(Couple.of(version, version));
//		}
//
//		String[] dummyPluginIds = new String[]{
//				"private",
//				"protected",
//				"public",
//				"internal",
//				"local"
//		};
//
//		Set<Couple<Integer>> toCheckPluginVersions = new LinkedHashSet<>();
//
//		for(String dummyPluginId : dummyPluginIds)
//		{
//			for(int i = 0; i <= count; i++)
//			{
//				for(Integer platformVersion : platformVersions)
//				{
//					PluginNode pluginNode = new PluginNode();
//					pluginNode.version = String.valueOf(i);
//					pluginNode.platformVersion = String.valueOf(platformVersion);
//					pluginNode.id = dummyPluginId;
//
//					channel._add(pluginNode);
//				}
//			}
//		}
//
//		// generate valid plugin version for each valid platform
//		for(Couple<Integer> toCheckPlatformVersion : toCheckPlatformVersions)
//		{
//			for(int i = 0; i < PluginChannelIterationService.ourMaxBuildCount; i++)
//			{
//				int version = count - i;
//
//				toCheckPluginVersions.add(Couple.of(toCheckPlatformVersion.getFirst(), version));
//			}
//		}
//
//		// do it!
//		myPluginChannelIterationService.cleanup(pluginChannel);
//
//		Set<String> toCheckIds = new LinkedHashSet<>();
//		toCheckIds.add(platformId);
//		Collections.addAll(toCheckIds, dummyPluginIds);
//
//		Map<String, OldPluginsState> states = channel.copyPluginsState();
//
//		// test data after
//		for(String toCheckId : toCheckIds)
//		{
//			// couple with platformVersion + pluginVersion
//			boolean platform = false;
//			Set<Couple<Integer>> targetVerCheck;
//			if(platformId.equals(toCheckId))
//			{
//				targetVerCheck = toCheckPlatformVersions;
//				platform = true;
//			}
//			else
//			{
//				targetVerCheck = toCheckPluginVersions;
//			}
//
//			OldPluginsState platforPluginsState = states.get(toCheckId);
//
//			assertNotNull(platforPluginsState);
//
//			NavigableMap<String, NavigableSet<PluginNode>> map = platforPluginsState.getPluginsByPlatformVersion();
//
//			if(platform)
//			{
//				// max + boot
//				assertEquals(PluginChannelIterationService.ourMaxBuildCount + 1, map.size());
//			}
//
//			for(Couple<Integer> couple : targetVerCheck)
//			{
//				NavigableSet<PluginNode> nodes = map.get(String.valueOf(couple.getFirst()));
//
//				// platform contains only one selft build
//				assertEquals(platform ? 1 : PluginChannelIterationService.ourMaxBuildCount, nodes.size());
//
//				assertInPluginChannel(nodes, toCheckId, String.valueOf(couple.getFirst()), couple.getSecond());
//			}
//		}
//	}

    private void assertInPluginChannel(Collection<PluginNode> all, String pluginId, String platformVersion, int version) {
        Optional<PluginNode> node = all.stream().filter(it -> it.platformVersion.equals(platformVersion) && it.id.equals(pluginId) && it.version.equals(String.valueOf(version))).findFirst();

        assertTrue("Version " + pluginId + ":" + platformVersion + ":" + version + " is not found", node.isPresent());
    }

    @Test
    public void testPlatformIteration() throws Exception {
        PluginNode platformNode = deployPlatform(PluginChannel.nightly, 1554, RepositoryUtil.ourStandardWinId, "/consulo-win-no-jre_1554.tar.gz");

        myPluginChannelIterationService.iterate(PluginChannel.nightly, PluginChannel.alpha);

        RepositoryChannelStore pluginChannelService = myPluginChannelsService.getRepositoryByChannel(PluginChannel.alpha);

        PluginNode pluginNodeInAlpha = pluginChannelService.select(platformNode.platformVersion, platformNode.id, null, false);
        assertNotNull(pluginNodeInAlpha);
        assertEquals(pluginNodeInAlpha.id, platformNode.id);
        assertNotNull(pluginNodeInAlpha);
        assertNotNull(pluginNodeInAlpha.targetPath);
        assertTrue(Files.exists(pluginNodeInAlpha.targetPath));
    }

    @Nonnull
    private PluginNode deployPlatform(PluginChannel channel, int platformVersion, String pluginId, String pluginPath) throws Exception {
        InputStream resourceAsStream = AnalyzerTest.class.getResourceAsStream(pluginPath);

        File tempFile = myFileService.createTempFile("platformTemp", ".tar.gz");
        try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
            FileUtil.copy(resourceAsStream, outputStream);
        }

        return myDeployService.deployPlatform(channel, null, platformVersion, pluginId, tempFile.toPath());
    }

    @Test
    public void testPluginIteration() throws Exception {
        PluginNode pluginNode = deployPlugin(PluginChannel.nightly, "com.intellij.xml");

        myPluginChannelIterationService.iterate(PluginChannel.nightly, PluginChannel.alpha);

        RepositoryChannelStore pluginChannelService = myPluginChannelsService.getRepositoryByChannel(PluginChannel.alpha);

        PluginNode pluginNodeInAlpha = pluginChannelService.select(pluginNode.platformVersion, pluginNode.id, null, false);
        assertNotNull(pluginNodeInAlpha);
        assertEquals(pluginNodeInAlpha.id, pluginNode.id);
        assertNotNull(pluginNodeInAlpha);
        assertNotNull(pluginNodeInAlpha.targetPath);
        assertTrue(Files.exists(pluginNodeInAlpha.targetPath));
    }

    private PluginNode deployPlugin(PluginChannel channel, String... pluginIds) throws Exception {
        PluginNode lastNode = null;
        for (String pluginId : pluginIds) {
            URL url = new URL("https://api.consulo.io/repository/download?id=" + pluginId + "&platformVersion=SNAPSHOT&version=SNAPSHOT&channel=nightly");

            InputStream resourceAsStream = url.openStream();

            lastNode = myDeployService.deployPlugin(channel, () -> resourceAsStream);
        }
        return lastNode;
    }
}
