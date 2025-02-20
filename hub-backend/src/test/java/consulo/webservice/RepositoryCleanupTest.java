package consulo.webservice;

import consulo.hub.backend.impl.WorkDirectoryServiceImpl;
import consulo.hub.backend.repository.cleanup.RepositoryCleanupService;
import consulo.hub.backend.repository.impl.store.BaseRepositoryChannelStore;
import consulo.hub.backend.repository.impl.store.neww.NewRepositoryChannelsService;
import consulo.hub.shared.repository.PluginChannel;
import consulo.hub.shared.repository.PluginNode;
import consulo.hub.shared.repository.util.PlatformNodeDesc;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.util.FileSystemUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

/**
 * @author VISTALL
 * @since 2025-02-19
 */
public class RepositoryCleanupTest extends Assert {
    private static final String PLATFORM_ID = PlatformNodeDesc.values().iterator().next().id();

    private static Path ourTempDir;
    private static NewRepositoryChannelsService ourPluginChannelsService;
    private static RepositoryCleanupService ourRepositoryCleanupService;

    @BeforeClass
    public static void before() throws Exception {
        ourTempDir = Files.createTempDirectory("webService");

        FileSystemUtils.deleteRecursively(ourTempDir);

        WorkDirectoryServiceImpl workDirectoryService = new WorkDirectoryServiceImpl(ourTempDir.toAbsolutePath().toString());
        workDirectoryService.init();

        SyncTempFileServiceImpl tempFileService = new SyncTempFileServiceImpl(workDirectoryService);
        tempFileService.init();

        ourPluginChannelsService = new NewRepositoryChannelsService(workDirectoryService, tempFileService, Runnable::run);

        ourPluginChannelsService.init();

        ourRepositoryCleanupService = new RepositoryCleanupService(ourPluginChannelsService);

        int maxVersion = 10000;
        List<Integer> versions = IntStream.range(1, maxVersion).mapToObj(Integer::valueOf).toList();

        Map<PluginChannel, List<Integer>> deployVersions = new TreeMap<>();

        for (PluginChannel channel : PluginChannel.values()) {
            if (channel == PluginChannel.nightly) {
                deployVersions.put(channel, versions);
            }
            else {
                List<Integer> anotherVersions = new ArrayList<>(versions);
                Collections.shuffle(anotherVersions);

                anotherVersions = anotherVersions.subList(0, RandomGenerator.getDefault().nextInt(maxVersion));

                deployVersions.put(channel, anotherVersions);
            }
        }

        for (Map.Entry<PluginChannel, List<Integer>> entry : deployVersions.entrySet()) {
            PluginChannel channel = entry.getKey();
            List<Integer> toDeploy = entry.getValue();


            BaseRepositoryChannelStore store = (BaseRepositoryChannelStore) ourPluginChannelsService.getRepositoryByChannel(channel);

            for (Integer deployVersion : toDeploy) {
                PluginNode pluginNode = new PluginNode();
                pluginNode.id = PLATFORM_ID;
                pluginNode.version = String.valueOf(deployVersion);
                pluginNode.platformVersion = String.valueOf(deployVersion);

                store._add(pluginNode);
            }
        }
    }

    @AfterClass
    public static void after() throws Exception {
        FileSystemUtils.deleteRecursively(ourTempDir);
    }

    @Test
    public void test() {
        ourRepositoryCleanupService.collect();
    }
}
