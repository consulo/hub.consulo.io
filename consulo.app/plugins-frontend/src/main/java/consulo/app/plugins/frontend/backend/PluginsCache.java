package consulo.app.plugins.frontend.backend;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import consulo.app.plugins.frontend.service.TagsLocalizeLoader;
import consulo.app.plugins.frontend.sitemap.JsonLd;
import consulo.app.plugins.frontend.sitemap.SitemapCacheService;
import consulo.hub.shared.repository.PluginNode;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author VISTALL
 * @since 2025-05-11
 */
public record PluginsCache(List<PluginNode> sortedByDownloads,
                           Map<String, PluginNode> mappped,
                           Map<String, String> jsonLdCache) {
    public boolean isValid() {
        return !sortedByDownloads().isEmpty();
    }

    public String getJsonLd(String pluginId,
                            ObjectMapper objectMapper,
                            SitemapCacheService sitemapCacheService,
                            TagsLocalizeLoader tagsLocalizeLoader) {
        PluginNode node = mappped().get(pluginId);
        if (node == null) {
            return null;
        }

        String cached = jsonLdCache().get(pluginId);
        if (cached != null) {
            return cached;
        }

        JsonLd jsonLd = new JsonLd();
        jsonLd.name = node.name;
        jsonLd.headline = node.name;
        jsonLd.description = node.description;
        jsonLd.softwareVersion = node.version;
        jsonLd.url = sitemapCacheService.getPluginUrl(node);

        LocalDateTime pluginDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(node.date), ZoneOffset.UTC);
        jsonLd.dateModified = DateTimeFormatter.ISO_DATE_TIME.format(pluginDate);

        jsonLd.keywords = Arrays.stream(node.tags)
            .map(tagsLocalizeLoader::getTagLocalize)
            .collect(Collectors.joining(", "));

        try {
            String json = objectMapper.writeValueAsString(jsonLd);
            jsonLdCache().putIfAbsent(pluginId, json);
            return json;
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
