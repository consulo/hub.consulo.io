package consulo.app.plugins.frontend.sitemap;

import consulo.app.plugins.frontend.backend.PluginsCache;
import consulo.hub.shared.repository.PluginNode;
import consulo.hub.shared.repository.util.PlatformNodeDesc;
import jakarta.annotation.Nonnull;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * @author VISTALL
 * @since 2026-02-17
 */
@Service
public class SitemapCacheService {
    public record Sitemap(byte[] data, ZonedDateTime buildTime) {
    }

    private String mySitemapServerUrl;

    private volatile Sitemap mySitemap;

    public SitemapCacheService(@Value("${sitemap.server.url}") String sitemapServerUrl) {
        this.mySitemapServerUrl = sitemapServerUrl;
    }

    public void rebuild(PluginsCache cache) throws IOException {
        mySitemap = build(cache);
    }

    @Nonnull
    public Sitemap get() {
        return Objects.requireNonNull(mySitemap);
    }

    private Sitemap build(PluginsCache pluginsCache) throws IOException {
        Namespace namespace = Namespace.getNamespace("http://www.sitemaps.org/schemas/sitemap/0.9");

        Element urlset = new Element("urlset");
        urlset.setNamespace(namespace);

        for (PluginNode pluginNode : pluginsCache.mappped().values()) {
            if (PlatformNodeDesc.getNode(pluginNode.id) != null) {
                continue;
            }

            Element urlElement = new Element("url", namespace);
            urlset.addContent(urlElement);

            Element locElement = new Element("loc", namespace);
            urlElement.addContent(locElement);
            locElement.setText(mySitemapServerUrl + "/v/" + URLEncoder.encode(pluginNode.id, StandardCharsets.UTF_8) + "/" + URLEncoder.encode(pluginNode.name, StandardCharsets.UTF_8));
        }

        Document document = new Document(urlset);
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            outputter.output(document, outputStream);

            return new Sitemap(outputStream.toByteArray(), ZonedDateTime.now(ZoneOffset.UTC));
        }
    }
}
