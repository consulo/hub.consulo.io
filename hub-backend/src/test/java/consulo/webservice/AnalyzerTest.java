package consulo.webservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import consulo.hub.backend.impl.WorkDirectoryServiceImpl;
import consulo.hub.backend.repository.PluginDeployService;
import consulo.hub.backend.repository.RepositoryChannelStore;
import consulo.hub.backend.repository.RepositoryNodeState;
import consulo.hub.backend.repository.analyzer.PluginAnalyzerServiceImpl;
import consulo.hub.backend.repository.analyzer.externalProcess.ExternalProcessPluginAnalyzerRunnerFactory;
import consulo.hub.backend.repository.impl.store.neww.NewRepositoryChannelsService;
import consulo.hub.shared.repository.PluginChannel;
import consulo.hub.shared.repository.PluginNode;
import consulo.webservice.stub.StubGithubReleaseServiceImpl;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.util.FileSystemUtils;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author VISTALL
 * @since 20-Sep-16
 */
public class AnalyzerTest extends Assert {
    private static final String[] DOWNLOAD_PLUGINS =
        {
            "com.intellij.xml",
            "com.intellij.properties",
            "consulo.java",
            "consulo.devkit",
            "consulo.dotnet",
            "consulo.unity3d",
            "consulo.nodejs",
            "com.intellij.images",
            "org.intellij.groovy",
            "org.jetbrains.plugins.gradle",
            "com.intellij.git",
            "mobi.hsz.idea.gitignore",
            "org.jetbrains.idea.maven",
            "org.jetbrains.plugins.javaFX"
        };

    private static Path ourTempDir;
    private static NewRepositoryChannelsService ourPluginChannelsService;

    @BeforeClass
    public static void before() throws Exception {
        ourTempDir = Files.createTempDirectory("webService");

        FileSystemUtils.deleteRecursively(ourTempDir);

        WorkDirectoryServiceImpl workDirectoryService = new WorkDirectoryServiceImpl(ourTempDir.toAbsolutePath().toString());
        workDirectoryService.init();

        SyncTempFileServiceImpl tempFileService = new SyncTempFileServiceImpl(workDirectoryService);
        tempFileService.init();

        ourPluginChannelsService = new NewRepositoryChannelsService(workDirectoryService, tempFileService, Runnable::run);

        ObjectMapper objectMapper = new ObjectMapper();

        PluginAnalyzerServiceImpl pluginAnalyzerService = new PluginAnalyzerServiceImpl(tempFileService, new ExternalProcessPluginAnalyzerRunnerFactory(objectMapper, tempFileService));

        pluginAnalyzerService.run(new String[0]);

        PluginDeployService deploy = new PluginDeployService(tempFileService, pluginAnalyzerService, objectMapper, new StubPluginHistoryServiceImpl(), ourPluginChannelsService, new
            StubGithubReleaseServiceImpl());

        ourPluginChannelsService.init();

        for (String pluginId : DOWNLOAD_PLUGINS) {
            URL url = new URL("https://api.consulo.io/repository/download?id=" + pluginId + "&platformVersion=SNAPSHOT&version=SNAPSHOT&channel=nightly");

            System.out.println("Downloading " + url);

            InputStream resourceAsStream = url.openStream();

            deploy.deployPlugin(PluginChannel.nightly, () -> resourceAsStream);
        }
    }

    @AfterClass
    public static void after() throws Exception {
        FileSystemUtils.deleteRecursively(ourTempDir);
    }

    @Test
    public void testPermissions() throws Exception {
        PluginNode pluginNode = findPlugin("consulo.dotnet");

        for (PluginNode.Permission permission : pluginNode.permissions) {
            if (permission.type.equals("PROCESS_CREATE")) {
                return;
            }
        }
        throw new AssertionError("PROCESS_CREATE permission not found");
    }

    @Test
    public void testTags() throws Exception {
        PluginNode pluginNode = findPlugin("consulo.devkit");

        assertEquals(pluginNode.tags.length, 1);
        assertEquals(pluginNode.tags[0], "ide.framework");
    }

    @Test
    public void testJavaPlugin() throws Exception {
        assetExtensionPreview("consulo.java", "consulo.module.content.layer.ModuleExtensionProvider", "java");
        assetExtensionPreview("consulo.java", "consulo.execution.configuration.ConfigurationType", "JavaApplication");
    }

    @Test
    public void testMavenPlugin() throws Exception {
        assetExtensionPreview("org.jetbrains.idea.maven", "consulo.virtualFileSystem.fileType.FileTypeFactory", "*|pom");

        assetExtensionPreview("org.jetbrains.idea.maven", "consulo.execution.configuration.ConfigurationType", "MavenRunConfiguration");

        assetExtensionPreview("org.jetbrains.idea.maven", "consulo.module.content.layer.ModuleExtensionProvider", "maven");
    }

    @Test
    public void testXmlPlugin() throws Exception {
        assetExtensionPreview("com.intellij.xml", "consulo.virtualFileSystem.fileType.FileTypeFactory", "*|xml");
    }

    @Test
    public void testGradlePlugin() throws Exception {
        assetExtensionPreview("org.jetbrains.plugins.gradle", "consulo.execution.configuration.ConfigurationType", "GradleRunConfiguration");
    }

    @Test
    public void testDotIgnorePlugin() throws Exception {
        PluginNode pluginNode = findPlugin("mobi.hsz.idea.gitignore");

        assertEquals(pluginNode.id, "mobi.hsz.idea.gitignore");
        assertNotNull(pluginNode.extensionPreviews);
        assertEquals(pluginNode.extensionPreviews.length, 58);
    }

    @Test
    public void testJavaFxPlugin() throws Exception {
        assetExtensionPreview("org.jetbrains.plugins.javaFX", "consulo.compiler.artifact.ArtifactType", "javafx");
        assetExtensionPreview("org.jetbrains.plugins.javaFX", "consulo.virtualFileSystem.fileType.FileTypeFactory", "*|fxml");
    }

    @Test
    public void testGitPlugin() throws Exception {
        PluginNode pluginNode = findPlugin("com.intellij.git");

        assertEquals(pluginNode.id, "com.intellij.git");
        assetExtensionPreview("com.intellij.git", "consulo.versionControlSystem.VcsFactory", "Git");
    }

    @Test
    public void testImagesPlugin() throws Exception {
        PluginNode pluginNode = findPlugin("com.intellij.images");

        assertEquals(pluginNode.id, "com.intellij.images");
    }

    @Test
    public void testPluginIcon() throws Exception {
        PluginNode pluginNode = findPlugin("consulo.nodejs");

        assertEquals(pluginNode.id, "consulo.nodejs");
        assertNotNull(pluginNode.iconBytes);
    }

    @Test
    public void testDarkIconBytes() throws Exception {
        PluginNode pluginNode = findPlugin("consulo.unity3d");

        assertEquals(pluginNode.id, "consulo.unity3d");
        assertNotNull(pluginNode.iconDarkBytes);
    }

    private void assetExtensionPreview(String pluginId, String apiClass, String... requiredValues) throws Exception {
        if (requiredValues.length == 0) {
            throw new IllegalArgumentException();
        }

        Set<String> requiredClasses = new HashSet<>(Set.of(requiredValues));
        PluginNode plugin = findPlugin(pluginId);

        for (PluginNode.ExtensionPreview extensionPreview : plugin.extensionPreviews) {
            if (extensionPreview.apiClassName.equals(apiClass)) {
                requiredClasses.remove(extensionPreview.implId);
            }
        }

        assertTrue("Missed extensions for " + apiClass + " = " + requiredClasses, requiredClasses.isEmpty());
    }

    private PluginNode findPlugin(String pluginId) throws Exception {
        RepositoryChannelStore channelService = ourPluginChannelsService.getRepositoryByChannel(PluginChannel.nightly);

        RepositoryNodeState state = Objects.requireNonNull(channelService.getState(pluginId), "plugin: " + pluginId + " not found");
        return state.select("SNAPSHOT", "SNAPSHOT", false);
    }
}
