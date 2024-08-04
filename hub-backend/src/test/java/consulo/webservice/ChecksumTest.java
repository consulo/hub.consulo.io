package consulo.webservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import consulo.hub.backend.impl.WorkDirectoryServiceImpl;
import consulo.hub.backend.repository.PluginDeployService;
import consulo.hub.backend.repository.analyzer.PluginAnalyzerServiceImpl;
import consulo.hub.backend.repository.analyzer.builtin.BuiltinPluginAnalyzerRunnerFactory;
import consulo.hub.backend.repository.impl.store.neww.NewRepositoryChannelsService;
import consulo.hub.shared.repository.PluginChannel;
import consulo.hub.shared.repository.PluginNode;
import org.apache.commons.codec.binary.Hex;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.FileSystemUtils;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;

/**
 * @author VISTALL
 * @since 17/07/2021
 */
public class ChecksumTest extends Assert {
    @Test
    public void testCheckSum() throws Exception {
        PluginNode l1 = loadPlugin("consulo.java");

        PluginNode l2 = loadPlugin("consulo.java");

        assertArrayEquals(Files.readAllBytes(l1.targetPath), Files.readAllBytes(l2.targetPath));

        assertTrue(MessageDigest.isEqual(Hex.decodeHex(l1.checksum.md5.toCharArray()), Hex.decodeHex(l2.checksum.md5.toCharArray())));

        assertTrue(MessageDigest.isEqual(Hex.decodeHex(l1.checksum.sha_256.toCharArray()), Hex.decodeHex(l2.checksum.sha_256.toCharArray())));

        assertTrue(MessageDigest.isEqual(Hex.decodeHex(l1.checksum.sha3_256.toCharArray()), Hex.decodeHex(l2.checksum.sha3_256.toCharArray())));
    }

    private PluginNode loadPlugin(String... pluginIds) throws Exception {
        assertTrue(pluginIds.length != 0);

        Path tempDir = Files.createTempDirectory("checksum-test");

        WorkDirectoryServiceImpl workDirectoryService = new WorkDirectoryServiceImpl(tempDir.toAbsolutePath().toString());
        workDirectoryService.init();

        SyncTempFileServiceImpl tempFileService = new SyncTempFileServiceImpl(workDirectoryService);
        tempFileService.init();

        FileSystemUtils.deleteRecursively(tempDir);

        NewRepositoryChannelsService pluginChannelsService = new NewRepositoryChannelsService(workDirectoryService, tempFileService, Runnable::run);
        pluginChannelsService.init();

        ObjectMapper objectMapper = new ObjectMapper();

        PluginAnalyzerServiceImpl pluginAnalyzerService = new PluginAnalyzerServiceImpl(tempFileService, new BuiltinPluginAnalyzerRunnerFactory(objectMapper));

        PluginDeployService deploy = new PluginDeployService(tempFileService, pluginAnalyzerService, objectMapper, new EmptyPluginHistoryServiceImpl(), pluginChannelsService, new
            EmptyGithubReleaseServiceImpl());

        pluginChannelsService.init();

        PluginNode lastNode = null;
        for (String pluginId : pluginIds) {
            URL url = new URL("https://api.consulo.io/repository/download?id=" + pluginId + "&platformVersion=SNAPSHOT&version=SNAPSHOT&channel=nightly");

            InputStream resourceAsStream = url.openStream();

            lastNode = deploy.deployPlugin(PluginChannel.alpha, () -> resourceAsStream);
        }

        return lastNode;
    }
}
