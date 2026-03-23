package consulo.hub.backend.repository.external.homebrew;

import consulo.hub.backend.WorkDirectoryService;
import consulo.hub.backend.repository.PluginStatisticsService;
import consulo.hub.backend.repository.RepositoryChannelStore;
import consulo.hub.backend.repository.RepositoryChannelsService;
import consulo.hub.backend.repository.external.PackageRepositoryUtil;
import consulo.hub.backend.repository.external.VelocityRenderer;
import consulo.hub.shared.repository.PluginChannel;
import consulo.hub.shared.repository.PluginNode;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHRef;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTree;
import org.kohsuke.github.GHTreeBuilder;
import org.kohsuke.github.GitHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Background service that generates and maintains Homebrew formula files (.rb) for Consulo
 * plugins and macOS platform packages.
 *
 * @author VISTALL
 */
@Service
public class HomebrewFormulaStore {
    private static final Logger LOG = LoggerFactory.getLogger(HomebrewFormulaStore.class);

    private static final String FORMULA_DIR = "Formula";

    private static final Map<String, String> MAC_PLATFORM_TO_FORMULA = Map.of(
        "consulo.dist.mac64.no.jre",  "consulo-without-jdk",
        "consulo.dist.macA64.no.jre", "consulo-without-jdk",
        "consulo.dist.mac64",         "consulo-with-jdk",
        "consulo.dist.macA64",        "consulo-with-jdk"
    );

    private final WorkDirectoryService myWorkDirectoryService;
    private final RepositoryChannelsService myChannelsService;
    private final PluginStatisticsService myStatsService;
    private final TaskExecutor myTaskExecutor;
    private final VelocityRenderer myVelocity;

    private final String myGitHubToken;
    private final Map<PluginChannel, String> myGitHubRepos;

    private final Set<PluginChannel> myInitialGenDone = ConcurrentHashMap.newKeySet();
    private final Map<PluginChannel, Boolean> myPendingRegen = new ConcurrentHashMap<>();

    private volatile Path myTapsRoot;

    @Autowired
    public HomebrewFormulaStore(@Nonnull WorkDirectoryService workDirectoryService,
                                @Nonnull RepositoryChannelsService repositoryChannelsService,
                                @Nonnull PluginStatisticsService pluginStatisticsService,
                                @Nonnull TaskExecutor taskExecutor,
                                @Nonnull VelocityRenderer velocityRenderer,
                                @Value("${homebrew.tap.github.token:}") String gitHubToken,
                                @Value("${homebrew.tap.repo.release:}") String releaseRepo,
                                @Value("${homebrew.tap.repo.beta:}") String betaRepo,
                                @Value("${homebrew.tap.repo.alpha:}") String alphaRepo,
                                @Value("${homebrew.tap.repo.nightly:}") String nightlyRepo) {
        myWorkDirectoryService = workDirectoryService;
        myChannelsService = repositoryChannelsService;
        myStatsService = pluginStatisticsService;
        myTaskExecutor = taskExecutor;
        myVelocity = velocityRenderer;
        myGitHubToken = gitHubToken;

        Map<PluginChannel, String> repos = new EnumMap<>(PluginChannel.class);
        if (!releaseRepo.isBlank()) repos.put(PluginChannel.release, releaseRepo);
        if (!betaRepo.isBlank())    repos.put(PluginChannel.beta,    betaRepo);
        if (!alphaRepo.isBlank())   repos.put(PluginChannel.alpha,   alphaRepo);
        if (!nightlyRepo.isBlank()) repos.put(PluginChannel.nightly, nightlyRepo);
        myGitHubRepos = repos;
    }

    @PostConstruct
    public void init() throws IOException {
        Path tapsRoot = myWorkDirectoryService.getWorkingDirectory().resolve("homebrew-taps");
        Files.createDirectories(tapsRoot);
        myTapsRoot = tapsRoot;

        for (PluginChannel channel : PluginChannel.values()) {
            Files.createDirectories(formulaDir(channel));
            RepositoryChannelStore store = myChannelsService.getRepositoryByChannel(channel);
            store.addChangeListener(() -> scheduleRegenChannel(channel));
        }
    }

    @Scheduled(fixedDelay = 5000, initialDelay = 5000)
    public void triggerInitialRegen() {
        if (myInitialGenDone.size() == PluginChannel.values().length) {
            return;
        }
        for (PluginChannel channel : PluginChannel.values()) {
            if (!myInitialGenDone.contains(channel)
                && !PackageRepositoryUtil.isLoading(myChannelsService, channel)) {
                myInitialGenDone.add(channel);
                scheduleRegenChannel(channel);
            }
        }
    }

    @Nullable
    public String getPluginFormula(@Nonnull PluginChannel channel, @Nonnull String pluginId) {
        return readFile(formulaDir(channel).resolve("consulo-plugin-" + pluginId + ".rb"));
    }

    @Nullable
    public String getPlatformFormula(@Nonnull PluginChannel channel, @Nonnull String formulaName) {
        return readFile(formulaDir(channel).resolve(formulaName + ".rb"));
    }

    private void scheduleRegenChannel(@Nonnull PluginChannel channel) {
        if (myPendingRegen.putIfAbsent(channel, Boolean.TRUE) != null) {
            return;
        }
        myTaskExecutor.execute(() -> {
            try {
                regenChannel(channel);
            }
            finally {
                myPendingRegen.remove(channel);
            }
        });
    }

    private void regenChannel(@Nonnull PluginChannel channel) {
        if (PackageRepositoryUtil.isLoading(myChannelsService, channel)) {
            return;
        }

        Map<String, String> changedFiles = new LinkedHashMap<>();

        List<PluginNode> plugins = PackageRepositoryUtil.getLatestPlugins(myChannelsService, myStatsService, channel);
        for (PluginNode node : plugins) {
            String filename = "consulo-plugin-" + node.id + ".rb";
            String content = buildPluginFormula(channel, node);
            if (writeIfChanged(formulaDir(channel).resolve(filename), content)) {
                changedFiles.put(FORMULA_DIR + "/" + filename, content);
            }
        }

        List<PluginNode> macNodes = PackageRepositoryUtil.getMacPlatformNodes(myChannelsService, myStatsService, channel);
        for (PluginNode node : macNodes) {
            String formulaName = MAC_PLATFORM_TO_FORMULA.get(node.id);
            if (formulaName == null) continue;
            String filename = formulaName + ".rb";
            String content = buildPlatformFormula(node, formulaName);
            if (writeIfChanged(formulaDir(channel).resolve(filename), content)) {
                changedFiles.put(FORMULA_DIR + "/" + filename, content);
            }
        }

        if (!changedFiles.isEmpty()) {
            String repo = myGitHubRepos.get(channel);
            if (repo != null && !myGitHubToken.isBlank()) {
                githubBatchPush(repo, changedFiles, "Update formulas (" + changedFiles.size() + " changed)");
            }
        }
    }

    private void githubBatchPush(@Nonnull String repo,
                                 @Nonnull Map<String, String> changedFiles,
                                 @Nonnull String commitMessage) {
        try {
            GitHub github = GitHub.connectUsingOAuth(myGitHubToken);
            GHRepository ghRepo = github.getRepository(repo);

            String branch = ghRepo.getDefaultBranch();
            GHRef ref = ghRepo.getRef("heads/" + branch);
            String headSha = ref.getObject().getSha();
            String treeSha = ghRepo.getCommit(headSha).getTree().getSha();

            GHTreeBuilder treeBuilder = ghRepo.createTree().baseTree(treeSha);
            for (Map.Entry<String, String> entry : changedFiles.entrySet()) {
                treeBuilder.add(entry.getKey(), entry.getValue(), false);
            }
            GHTree newTree = treeBuilder.create();

            GHCommit newCommit = ghRepo.createCommit()
                .message(commitMessage)
                .tree(newTree.getSha())
                .parent(headSha)
                .create();

            ref.updateTo(newCommit.getSHA1());

            LOG.info("Pushed {} formula(s) to {}", changedFiles.size(), repo);
        }
        catch (Exception e) {
            LOG.warn("GitHub push failed for {}: {}", repo, e.getMessage());
        }
    }

    private String buildPluginFormula(@Nonnull PluginChannel channel, @Nonnull PluginNode node) {
        String formulaName = "consulo-plugin-" + node.id;
        String desc = node.description != null && !node.description.isBlank()
            ? node.description.trim().lines().findFirst().orElse(node.id)
            : (node.name != null ? node.name : node.id);
        String homepage = node.url != null && !node.url.isEmpty() ? node.url : "https://consulo.io";
        String url = resolveUrl(channel, node);
        String sha256 = node.checksum != null && node.checksum.sha_256 != null
            ? node.checksum.sha_256.toLowerCase() : null;
        List<String> deps = node.dependencies != null ? Arrays.asList(node.dependencies) : List.of();

        Map<String, Object> ctx = new HashMap<>();
        ctx.put("className", toRubyClassName(formulaName));
        ctx.put("desc", escapeRuby(desc));
        ctx.put("homepage", escapeRuby(homepage));
        ctx.put("version", escapeRuby(node.version));
        ctx.put("url", url != null ? escapeRuby(url) : null);
        ctx.put("sha256", sha256);
        ctx.put("deps", deps);
        return myVelocity.render("templates/pkg/homebrew-plugin.rb.vm", ctx);
    }

    private String buildPlatformFormula(@Nonnull PluginNode node, @Nonnull String formulaName) {
        String desc = node.name != null ? node.name : formulaName;
        String sha256 = node.checksum != null && node.checksum.sha_256 != null
            ? node.checksum.sha_256.toLowerCase() : null;
        String url = node.downloadUrls != null && node.downloadUrls.length > 0
            ? node.downloadUrls[0] : null;

        Map<String, Object> ctx = new HashMap<>();
        ctx.put("className", toRubyClassName(formulaName));
        ctx.put("desc", escapeRuby(desc));
        ctx.put("version", escapeRuby(node.version));
        ctx.put("url", url != null ? escapeRuby(url) : null);
        ctx.put("sha256", sha256);
        return myVelocity.render("templates/pkg/homebrew-platform.rb.vm", ctx);
    }

    private boolean writeIfChanged(@Nonnull Path path, @Nonnull String content) {
        try {
            byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
            if (Files.exists(path) && Arrays.equals(Files.readAllBytes(path), bytes)) {
                return false;
            }
            Files.createDirectories(path.getParent());
            Files.write(path, bytes);
            return true;
        }
        catch (IOException e) {
            LOG.warn("Failed to write formula {}: {}", path.getFileName(), e.getMessage());
            return false;
        }
    }

    @Nullable
    private String readFile(@Nonnull Path path) {
        if (!Files.exists(path)) return null;
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        }
        catch (IOException e) {
            LOG.warn("Failed to read formula {}: {}", path.getFileName(), e.getMessage());
            return null;
        }
    }

    @Nonnull
    private Path formulaDir(@Nonnull PluginChannel channel) {
        return myTapsRoot.resolve(channel.name()).resolve(FORMULA_DIR);
    }

    @Nullable
    private static String resolveUrl(@Nonnull PluginChannel channel, @Nonnull PluginNode node) {
        if (node.downloadUrls != null && node.downloadUrls.length > 0) return node.downloadUrls[0];
        if (node.targetPath != null || node.targetFile != null) {
            return "https://api.consulo.io/homebrew/" + channel.name()
                + "/formula/consulo-plugin-" + node.id + "-" + node.version + ".zip";
        }
        return null;
    }

    static String toRubyClassName(@Nonnull String formulaName) {
        StringBuilder sb = new StringBuilder();
        boolean capitalizeNext = true;
        for (char c : formulaName.toCharArray()) {
            if (c == '-' || c == '.') {
                capitalizeNext = true;
            }
            else if (capitalizeNext) {
                sb.append(Character.toUpperCase(c));
                capitalizeNext = false;
            }
            else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static String escapeRuby(@Nonnull String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
