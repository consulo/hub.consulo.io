package consulo.webservice;

import consulo.hub.backend.impl.WorkDirectoryServiceImpl;
import consulo.hub.backend.repository.RepositoryChannelStore;
import consulo.hub.backend.repository.cleanup.RepositoryCleanupService;
import consulo.hub.backend.repository.impl.store.BaseRepositoryChannelStore;
import consulo.hub.backend.repository.impl.store.neww.NewRepositoryChannelsService;
import consulo.hub.shared.repository.PluginChannel;
import consulo.hub.shared.repository.PluginNode;
import consulo.hub.shared.repository.util.PlatformNodeDesc;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author VISTALL
 * @since 2025-02-19
 */
public class RepositoryCleanupTest extends Assert {
    private static final String PLATFORM_ID = PlatformNodeDesc.values().iterator().next().id();
    private static final String PLUGIN_ID = "org.example.plugin";

    private static final PluginChannel[] RELEASE_CYCLE_CHANNELS = {
        PluginChannel.release,
        PluginChannel.beta,
        PluginChannel.alpha,
        PluginChannel.nightly
    };

    private Path myTempDir;
    private Path myArtifactsDir;
    private NewRepositoryChannelsService myChannelsService;
    private RepositoryCleanupService myCleanupService;

    @Before
    public void before() throws Exception {
        myTempDir = Files.createTempDirectory("repositoryCleanupTest");

        WorkDirectoryServiceImpl workDirectoryService = new WorkDirectoryServiceImpl(myTempDir.toAbsolutePath().toString());
        workDirectoryService.init();

        SyncTempFileServiceImpl tempFileService = new SyncTempFileServiceImpl(workDirectoryService);
        tempFileService.init();

        myChannelsService = new NewRepositoryChannelsService(workDirectoryService, tempFileService, Runnable::run);
        myChannelsService.init();

        myArtifactsDir = Files.createDirectories(myTempDir.resolve("artifacts"));

        myCleanupService = new RepositoryCleanupService(myChannelsService, Runnable::run);
    }

    @After
    public void after() throws Exception {
        FileSystemUtils.deleteRecursively(myTempDir);
    }

    @Test
    public void testValhallaOnlyArtifactsPurged() throws Exception {
        Assume.assumeFalse(RepositoryCleanupService.VALHALLA_ENABLED);

        deployPlatform(PluginChannel.valhalla, 100);
        deployPlatform(PluginChannel.nightly, 200);

        myCleanupService.runCleanUp();

        assertFalse(Files.exists(artifactPath(PLATFORM_ID, "100")));
        assertFalse(Files.exists(jsonPath(PLATFORM_ID, "100")));
        assertFalse(channel(PluginChannel.valhalla).isInRepository(PLATFORM_ID, "100", "100"));

        assertTrue(Files.exists(artifactPath(PLATFORM_ID, "200")));
        assertTrue(channel(PluginChannel.nightly).isInRepository(PLATFORM_ID, "200", "200"));
    }

    @Test
    public void testValhallaSharedArtifactNotDeleted() throws Exception {
        Assume.assumeFalse(RepositoryCleanupService.VALHALLA_ENABLED);

        deployPlatform(PluginChannel.valhalla, 300);
        deployPlatform(PluginChannel.nightly, 300);

        myCleanupService.runCleanUp();

        assertTrue(Files.exists(artifactPath(PLATFORM_ID, "300")));
        assertTrue(Files.exists(jsonPath(PLATFORM_ID, "300")));
        assertFalse(channel(PluginChannel.valhalla).isInRepository(PLATFORM_ID, "300", "300"));
        assertTrue(channel(PluginChannel.nightly).isInRepository(PLATFORM_ID, "300", "300"));
    }

    @Test
    public void testValhallaDoesNotBlockReleaseCycleCleanup() throws Exception {
        for (int build = 1; build <= 30; build++) {
            for (PluginChannel channel : RELEASE_CYCLE_CHANNELS) {
                deployPlatform(channel, build);
            }
        }

        // disjoint valhalla numbering must not poison the fully-released analysis
        deployPlatform(PluginChannel.valhalla, 9999);

        deployPlugin(PluginChannel.nightly, "501", 1);
        deployPlugin(PluginChannel.nightly, "530", 30);

        myCleanupService.runCleanUp();

        for (int build = 1; build <= 5; build++) {
            String version = String.valueOf(build);
            assertFalse("platform build " + build + " must be removed", Files.exists(artifactPath(PLATFORM_ID, version)));
            assertFalse(channel(PluginChannel.nightly).isInRepository(PLATFORM_ID, version, version));
            assertFalse(channel(PluginChannel.release).isInRepository(PLATFORM_ID, version, version));
        }

        for (int build = 6; build <= 30; build++) {
            String version = String.valueOf(build);
            assertTrue("platform build " + build + " must be kept", Files.exists(artifactPath(PLATFORM_ID, version)));
            assertTrue(channel(PluginChannel.nightly).isInRepository(PLATFORM_ID, version, version));
        }

        assertFalse("plugin for removed platform build must be removed", Files.exists(artifactPath(PLUGIN_ID, "501")));
        assertFalse(channel(PluginChannel.nightly).isInRepository(PLUGIN_ID, "501", "1"));

        assertTrue("plugin for kept platform build must be kept", Files.exists(artifactPath(PLUGIN_ID, "530")));
        assertTrue(channel(PluginChannel.nightly).isInRepository(PLUGIN_ID, "530", "30"));
    }

    @Test
    public void testNotFullyReleasedBuildKept() throws Exception {
        // nightly-only build - still in propagation, must survive cleanup
        deployPlatform(PluginChannel.nightly, 999);

        for (int build = 1; build <= 30; build++) {
            for (PluginChannel channel : RELEASE_CYCLE_CHANNELS) {
                deployPlatform(channel, build);
            }
        }

        myCleanupService.runCleanUp();

        assertTrue(Files.exists(artifactPath(PLATFORM_ID, "999")));
        assertTrue(channel(PluginChannel.nightly).isInRepository(PLATFORM_ID, "999", "999"));

        assertFalse(Files.exists(artifactPath(PLATFORM_ID, "1")));
    }

    @Test
    public void testRemoveLimitPerSession() throws Exception {
        for (int build = 1; build <= 200; build++) {
            for (PluginChannel channel : RELEASE_CYCLE_CHANNELS) {
                deployPlatform(channel, build);
            }
        }

        myCleanupService.runCleanUp();

        for (int build = 1; build <= 100; build++) {
            assertFalse("platform build " + build + " must be removed", Files.exists(artifactPath(PLATFORM_ID, String.valueOf(build))));
        }

        for (int build = 101; build <= 200; build++) {
            assertTrue("platform build " + build + " must be kept", Files.exists(artifactPath(PLATFORM_ID, String.valueOf(build))));
        }
    }

    @Test
    public void testNothingRemovedBelowLimit() throws Exception {
        for (int build = 1; build <= RepositoryCleanupService.ourMaxBuildCount; build++) {
            for (PluginChannel channel : RELEASE_CYCLE_CHANNELS) {
                deployPlatform(channel, build);
            }
        }

        myCleanupService.runCleanUp();

        for (int build = 1; build <= RepositoryCleanupService.ourMaxBuildCount; build++) {
            assertTrue(Files.exists(artifactPath(PLATFORM_ID, String.valueOf(build))));
        }
    }

    private void deployPlatform(PluginChannel channel, int build) throws IOException {
        deploy(channel, PLATFORM_ID, String.valueOf(build), String.valueOf(build));
    }

    private void deployPlugin(PluginChannel channel, String version, int platformBuild) throws IOException {
        deploy(channel, PLUGIN_ID, version, String.valueOf(platformBuild));
    }

    private void deploy(PluginChannel channel, String id, String version, String platformVersion) throws IOException {
        PluginNode node = new PluginNode();
        node.id = id;
        node.version = version;
        node.platformVersion = platformVersion;
        node.targetPath = artifactPath(id, version);

        if (!Files.exists(node.targetPath)) {
            Files.createFile(node.targetPath);
            Files.createFile(jsonPath(id, version));
        }

        BaseRepositoryChannelStore store = (BaseRepositoryChannelStore) myChannelsService.getRepositoryByChannel(channel);
        store._add(node);
    }

    private Path artifactPath(String id, String version) {
        return myArtifactsDir.resolve(id + "_" + version + ".tar.gz");
    }

    private Path jsonPath(String id, String version) {
        return myArtifactsDir.resolve(id + "_" + version + ".tar.gz.json");
    }

    private RepositoryChannelStore channel(PluginChannel channel) {
        return myChannelsService.getRepositoryByChannel(channel);
    }
}
