package consulo.webservice;

import consulo.hub.backend.impl.WorkDirectoryServiceImpl;
import consulo.hub.backend.repository.PluginStatisticsService;
import consulo.hub.backend.repository.external.DistributionRepository;
import consulo.hub.backend.repository.external.RepositoryIndexService;
import consulo.hub.backend.repository.external.VelocityRenderer;
import consulo.hub.backend.repository.external.apt.AptDistributionRepository;
import consulo.hub.backend.repository.external.pacman.PacmanDistributionRepository;
import consulo.hub.backend.repository.external.rpm.RpmDistributionRepository;
import consulo.hub.backend.repository.external.winget.WingetDistributionRepository;
import consulo.hub.backend.repository.impl.store.BaseRepositoryChannelStore;
import consulo.hub.backend.repository.impl.store.neww.NewRepositoryChannelsService;
import consulo.hub.shared.repository.PluginChannel;
import consulo.hub.shared.repository.PluginNode;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.util.FileSystemUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Tests that the distribution repositories correctly generate APT, RPM, Pacman, and WinGet
 * indexes from Velocity templates after a plugin is added to a channel.
 *
 * @author VISTALL
 */
public class RepositoryIndexServiceTest extends Assert {
    private static final String TEST_PLUGIN_ID = "com.intellij.git";
    private static final String TEST_VERSION = "2430";
    private static final String TEST_PLATFORM_VERSION = "251";

    private static Path ourTempDir;

    private static AptDistributionRepository ourAptRepo;
    private static RpmDistributionRepository ourRpmRepo;
    private static PacmanDistributionRepository ourPacmanRepo;
    private static WingetDistributionRepository ourWingetRepo;

    @BeforeClass
    public static void before() throws Exception {
        ourTempDir = Files.createTempDirectory("repositoryIndexService");

        FileSystemUtils.deleteRecursively(ourTempDir);

        WorkDirectoryServiceImpl workDirectoryService = new WorkDirectoryServiceImpl(ourTempDir.toAbsolutePath().toString());
        workDirectoryService.init();

        SyncTempFileServiceImpl tempFileService = new SyncTempFileServiceImpl(workDirectoryService);
        tempFileService.init();

        NewRepositoryChannelsService channelsService = new NewRepositoryChannelsService(workDirectoryService, tempFileService, Runnable::run);
        channelsService.init();

        // Add a test plugin node to the nightly channel
        BaseRepositoryChannelStore store = (BaseRepositoryChannelStore) channelsService.getRepositoryByChannel(PluginChannel.nightly);

        PluginNode pluginNode = new PluginNode();
        pluginNode.id = TEST_PLUGIN_ID;
        pluginNode.version = TEST_VERSION;
        pluginNode.platformVersion = TEST_PLATFORM_VERSION;
        pluginNode.name = "Git Integration";
        pluginNode.description = "Provides Git VCS integration.";
        store._add(pluginNode);

        // PluginStatisticsService is safe to instantiate with null outside Spring —
        // @PostConstruct does not fire, myStatistics stays empty, returns 0 for all counts
        PluginStatisticsService statsService = new PluginStatisticsService(null);

        VelocityRenderer velocityRenderer = new VelocityRenderer();

        ourAptRepo = new AptDistributionRepository(channelsService, statsService, velocityRenderer);
        ourRpmRepo = new RpmDistributionRepository(channelsService, statsService, velocityRenderer);
        ourPacmanRepo = new PacmanDistributionRepository(channelsService, statsService, velocityRenderer);
        ourWingetRepo = new WingetDistributionRepository(channelsService, statsService);

        List<DistributionRepository<?>> repos = List.of(ourAptRepo, ourRpmRepo, ourPacmanRepo, ourWingetRepo);

        RepositoryIndexService indexService = new RepositoryIndexService(channelsService, task -> task.run(), repos);
        indexService.init();
        indexService.triggerInitialRegen();
    }

    @AfterClass
    public static void after() throws Exception {
        FileSystemUtils.deleteRecursively(ourTempDir);
    }

    // ---- APT ----

    @Test
    public void testAptIndexGenerated() {
        assertNotNull("APT index should be generated for nightly channel",
            ourAptRepo.getIndex(PluginChannel.nightly));
    }

    @Test
    public void testAptReleaseContainsChannel() {
        AptDistributionRepository.AptIndex index = ourAptRepo.getIndex(PluginChannel.nightly);
        assertNotNull(index);
        assertTrue("Release file should contain suite nightly", index.release().contains("Suite: nightly"));
        assertTrue("Release file should list architectures", index.release().contains("Architectures:"));
        assertTrue("Release file should have SHA256 section", index.release().contains("SHA256:"));
    }

    @Test
    public void testAptPackagesContainsPlugin() {
        AptDistributionRepository.AptIndex index = ourAptRepo.getIndex(PluginChannel.nightly);
        assertNotNull(index);
        String packages = new String(index.packagesAll(), StandardCharsets.UTF_8);
        assertTrue("Packages file should contain plugin package name",
            packages.contains("consulo-plugin-" + TEST_PLUGIN_ID));
        assertTrue("Packages file should contain plugin version",
            packages.contains("Version: " + TEST_VERSION));
    }

    @Test
    public void testAptPackagesGzNotEmpty() {
        AptDistributionRepository.AptIndex index = ourAptRepo.getIndex(PluginChannel.nightly);
        assertNotNull(index);
        assertNotNull("Packages.gz should not be null", index.packagesAllGz());
        assertTrue("Packages.gz should be non-empty", index.packagesAllGz().length > 0);
    }

    // ---- RPM ----

    @Test
    public void testRpmIndexGenerated() {
        assertNotNull("RPM index should be generated for nightly channel",
            ourRpmRepo.getIndex(PluginChannel.nightly));
    }

    @Test
    public void testRpmRepomdIsValid() {
        RpmDistributionRepository.RpmIndex index = ourRpmRepo.getIndex(PluginChannel.nightly);
        assertNotNull(index);
        assertTrue("repomd.xml should contain repomd element", index.repomd().contains("<repomd"));
        assertTrue("repomd.xml should reference primary", index.repomd().contains("primary"));
    }

    @Test
    public void testRpmPrimaryGzNotEmpty() {
        RpmDistributionRepository.RpmIndex index = ourRpmRepo.getIndex(PluginChannel.nightly);
        assertNotNull(index);
        assertNotNull("primary.xml.gz should not be null", index.primaryXmlGz());
        assertTrue("primary.xml.gz should be non-empty", index.primaryXmlGz().length > 0);
    }

    // ---- Pacman ----

    @Test
    public void testPacmanDbGenerated() {
        byte[] db = ourPacmanRepo.getIndex(PluginChannel.nightly);
        assertNotNull("Pacman DB should be generated for nightly channel", db);
        assertTrue("Pacman DB should be non-empty", db.length > 0);
    }

    // ---- WinGet ----

    @Test
    public void testWingetIndexGenerated() {
        WingetDistributionRepository.WingetIndex index = ourWingetRepo.getIndex(PluginChannel.nightly);
        assertNotNull("WinGet index should be generated for nightly channel", index);
    }

    @Test
    public void testWingetSearchEntriesContainsPlugin() {
        WingetDistributionRepository.WingetIndex index = ourWingetRepo.getIndex(PluginChannel.nightly);
        assertNotNull(index);
        assertFalse("WinGet search entries should not be empty", index.searchEntries().isEmpty());
        boolean found = index.searchEntries().stream().anyMatch(e -> {
            Object id = e.get("PackageIdentifier");
            return id != null && id.toString().contains(TEST_PLUGIN_ID.toLowerCase());
        });
        assertTrue("WinGet search entries should contain the test plugin", found);
    }

    // ---- all channels ----

    @Test
    public void testAllChannelsGenerated() {
        for (PluginChannel channel : PluginChannel.values()) {
            assertNotNull("APT index should be generated for channel " + channel,
                ourAptRepo.getIndex(channel));
            assertNotNull("RPM index should be generated for channel " + channel,
                ourRpmRepo.getIndex(channel));
            assertNotNull("Pacman DB should be generated for channel " + channel,
                ourPacmanRepo.getIndex(channel));
            assertNotNull("WinGet index should be generated for channel " + channel,
                ourWingetRepo.getIndex(channel));
        }
    }
}
