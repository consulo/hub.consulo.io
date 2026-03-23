package consulo.hub.backend.repository.external.winget;

import com.fasterxml.jackson.databind.JsonNode;
import consulo.hub.shared.repository.PluginChannel;
import consulo.hub.shared.repository.PluginNode;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Serves a WinGet REST source for Consulo plugins and platform packages (Windows).
 * <p>
 * Usage:
 * <pre>
 * winget source add --name consulo --arg https://api.consulo.io/api/winget/release --type Microsoft.Rest
 * </pre>
 *
 * @author VISTALL
 */
@RestController
@RequestMapping("/api/winget")
public class WingetRepositoryController {

    public static final Map<String, String[]> WINDOWS_PLATFORM_TO_WINGET;

    static {
        Map<String, String[]> m = new LinkedHashMap<>();
        m.put("consulo.dist.windows64.installer", new String[]{"consulo.with-jdk",    "x64",   "exe"});
        m.put("consulo.dist.windows64.zip",        new String[]{"consulo.with-jdk",    "x64",   "zip"});
        m.put("consulo.dist.windows.zip",           new String[]{"consulo.with-jdk",    "x86",   "zip"});
        m.put("consulo.dist.windows.aarch64.zip",   new String[]{"consulo.with-jdk",    "arm64", "zip"});
        m.put("consulo.dist.windows.no.jre.zip",    new String[]{"consulo.without-jdk", "x64",   "zip"});
        WINDOWS_PLATFORM_TO_WINGET = Collections.unmodifiableMap(m);
    }

    static final String PUBLISHER     = "Consulo";
    static final String PUBLISHER_URL = "https://consulo.io";
    static final String PACKAGE_URL   = "https://consulo.io";
    static final String LOCALE        = "en-US";
    static final String LICENSE       = "Apache-2.0";

    private final WingetDistributionRepository myWingetRepository;

    @Autowired
    public WingetRepositoryController(WingetDistributionRepository wingetRepository) {
        myWingetRepository = wingetRepository;
    }

    @GetMapping("/{channel}/information")
    public ResponseEntity<Map<String, Object>> information(@PathVariable PluginChannel channel) {
        return ResponseEntity.ok(Map.of(
            "Data", Map.of(
                "SourceIdentifier", "consulo-" + channel.name(),
                "ServerSupportedVersions", List.of("1.1.0")
            )
        ));
    }

    @PostMapping(value = "/{channel}/manifestSearch", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> search(@PathVariable PluginChannel channel,
                                                      @RequestBody(required = false) JsonNode body) {
        WingetDistributionRepository.WingetIndex index = myWingetRepository.getIndex(channel);
        if (index == null) return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();

        String exactIdentifier = extractExactIdentifier(body);
        String keyword = extractKeyword(body);

        List<Map<String, Object>> results = new ArrayList<>();
        for (Map<String, Object> entry : index.searchEntries()) {
            String identifier = (String) entry.get("PackageIdentifier");
            String name = (String) entry.get("PackageName");
            if (matches(identifier, name, exactIdentifier, keyword)) {
                results.add(entry);
            }
        }

        return ResponseEntity.ok(Map.of("Data", results));
    }

    @GetMapping("/{channel}/packageManifests/{packageIdentifier}")
    public ResponseEntity<Map<String, Object>> manifest(@PathVariable PluginChannel channel,
                                                        @PathVariable String packageIdentifier) {
        WingetDistributionRepository.WingetIndex index = myWingetRepository.getIndex(channel);
        if (index == null) return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();

        Map<String, Object> versionEntry = index.manifests().get(packageIdentifier.toLowerCase());
        if (versionEntry == null) return ResponseEntity.notFound().build();

        return ok(Map.of(
            "PackageIdentifier", packageIdentifier,
            "Versions", List.of(versionEntry)
        ));
    }

    static Map<String, Object> searchEntry(String identifier, String name, String version) {
        return Map.of(
            "PackageIdentifier", identifier,
            "PackageName", name,
            "Publisher", PUBLISHER,
            "Versions", List.of(Map.of("PackageVersion", version))
        );
    }

    static Map<String, Object> versionEntry(String version, String name, String desc,
                                              List<Map<String, Object>> installers) {
        Map<String, Object> locale = new LinkedHashMap<>();
        locale.put("PackageLocale", LOCALE);
        locale.put("Publisher", PUBLISHER);
        locale.put("PublisherUrl", PUBLISHER_URL);
        locale.put("PackageName", name);
        locale.put("PackageUrl", PACKAGE_URL);
        locale.put("License", LICENSE);
        locale.put("ShortDescription", desc);

        Map<String, Object> ver = new LinkedHashMap<>();
        ver.put("PackageVersion", version);
        ver.put("DefaultLocale", locale);
        ver.put("Installers", installers);
        return ver;
    }

    static String platformName(@Nonnull String wingetId) {
        return wingetId.equals("consulo.with-jdk") ? "Consulo IDE (with JDK)" : "Consulo IDE (without JDK)";
    }

    @Nonnull
    static String shortDesc(@Nonnull PluginNode node) {
        if (node.description != null && !node.description.isBlank()) {
            return node.description.trim().split("\r?\n")[0].trim();
        }
        return node.name != null ? node.name : node.id;
    }

    private static boolean matches(@Nonnull String identifier, @Nullable String name,
                                    @Nullable String exactIdentifier, @Nullable String keyword) {
        if (exactIdentifier != null) return identifier.equalsIgnoreCase(exactIdentifier);
        if (keyword != null) {
            String kw = keyword.toLowerCase();
            return identifier.toLowerCase().contains(kw)
                || (name != null && name.toLowerCase().contains(kw));
        }
        return true;
    }

    @Nullable
    private static String extractExactIdentifier(@Nullable JsonNode body) {
        if (body == null) return null;
        JsonNode filters = body.get("Filters");
        if (filters != null && filters.isArray()) {
            for (JsonNode filter : filters) {
                JsonNode field = filter.get("PackageMatchField");
                if (field == null || !"PackageIdentifier".equalsIgnoreCase(field.asText())) continue;
                JsonNode match = filter.get("RequestMatch");
                if (match == null) continue;
                JsonNode matchType = match.get("MatchType");
                if (matchType != null && "Exact".equalsIgnoreCase(matchType.asText())) {
                    JsonNode kw = match.get("KeyWord");
                    if (kw != null) return kw.asText();
                }
            }
        }
        return null;
    }

    @Nullable
    private static String extractKeyword(@Nullable JsonNode body) {
        if (body == null) return null;
        JsonNode query = body.get("Query");
        if (query != null) {
            JsonNode kw = query.get("KeyWord");
            if (kw != null && !kw.isNull()) return kw.asText();
        }
        return null;
    }

    private static ResponseEntity<Map<String, Object>> ok(@Nonnull Map<String, Object> data) {
        return ResponseEntity.ok(Map.of("Data", data));
    }
}
