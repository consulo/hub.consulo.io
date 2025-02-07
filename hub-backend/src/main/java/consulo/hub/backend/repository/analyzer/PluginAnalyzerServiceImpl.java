package consulo.hub.backend.repository.analyzer;

import consulo.container.internal.plugin.PluginDescriptorImpl;
import consulo.container.plugin.PluginId;
import consulo.hub.backend.TempFileService;
import consulo.hub.backend.repository.PluginAnalyzerService;
import consulo.hub.backend.repository.RepositoryChannelStore;
import consulo.hub.backend.util.ZipUtil;
import consulo.hub.shared.repository.PluginNode;
import consulo.util.collection.ArrayUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.zip.ZipFile;

/**
 * @author VISTALL
 * @since 20-Sep-16
 */
@Service
@Order(2_000)
public class PluginAnalyzerServiceImpl implements CommandLineRunner, PluginAnalyzerService {
    private static final Logger LOG = LoggerFactory.getLogger(PluginAnalyzerServiceImpl.class);

    private final TempFileService myTempFileService;

    private final PluginAnalyzerRunnerFactory myPluginAnalyzerRunnerFactory;

    private final PluginAnalyzerEnv myPluginAnalyzerEnv;

    @Autowired
    public PluginAnalyzerServiceImpl(TempFileService tempFileService, PluginAnalyzerRunnerFactory pluginAnalyzerRunnerFactory) {
        myTempFileService = tempFileService;
        myPluginAnalyzerRunnerFactory = pluginAnalyzerRunnerFactory;
        myPluginAnalyzerEnv = new PluginAnalyzerEnv(tempFileService);
    }

    @Override
    public void run(String[] args) throws Exception {
        myPluginAnalyzerEnv.init();
    }

    @Override
    @Nonnull
    public PluginNode.ExtensionPreview[] analyze(File deployHome, PluginDescriptorImpl descriptor, RepositoryChannelStore channelService) throws Exception {
        File[] forRemove = new File[0];

        List<File> pluginDirs = new ArrayList<>();

        Set<String> dependencies = new HashSet<>();
        collectAllDependencies(descriptor.getPluginId().getIdString(), Arrays.stream(descriptor.getDependentPluginIds()).map(PluginId::getIdString).toArray(String[]::new), channelService,
            dependencies, new HashSet<>());

        if (!dependencies.isEmpty()) {
            File analyzeUnzip = myTempFileService.createTempFile("plugin_deps_" + descriptor.getPluginId(), "");
            forRemove = ArrayUtil.append(forRemove, analyzeUnzip);
            pluginDirs.add(analyzeUnzip);

            for (String dependencyId : dependencies) {
                PluginNode pluginNode = channelService.select(RepositoryChannelStore.SNAPSHOT, dependencyId, null, false);
                if (pluginNode == null) {
                    continue;
                }


                try (ZipFile zipFile = pluginNode.openZip()) {
                    ZipUtil.extract(zipFile, analyzeUnzip);
                }
            }
        }

        try {
            PluginAnalyzerRunner runner = myPluginAnalyzerRunnerFactory.create(myPluginAnalyzerEnv);

            pluginDirs.add(deployHome);

            return runner.run(descriptor.getPluginId().getIdString(), pluginDirs.stream().map(File::getPath).toArray(String[]::new));
        }
        finally {
            myTempFileService.asyncDelete(forRemove);
        }
    }

    private void collectAllDependencies(String pluginId, @Nullable String[] dependencies, RepositoryChannelStore channelService, Set<String> processed, Set<String> result) {
        if (!processed.add(pluginId)) {
            return;
        }

        if (dependencies == null) {
            return;
        }

        for (String dependencyId : dependencies) {
            PluginNode dependPlugin = channelService.select(RepositoryChannelStore.SNAPSHOT, dependencyId, null, false);
            if (dependPlugin == null) {
                continue;
            }

            result.add(dependencyId);

            collectAllDependencies(dependPlugin.id, dependPlugin.dependencies, channelService, processed, result);
        }
    }
}
