package consulo.app.plugins.frontend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.communication.IndexHtmlRequestListener;
import consulo.app.plugins.frontend.backend.PluginsCache;
import consulo.app.plugins.frontend.backend.PluginsCacheService;
import consulo.app.plugins.frontend.service.TagsLocalizeLoader;
import consulo.app.plugins.frontend.sitemap.SitemapCacheService;
import consulo.app.plugins.frontend.ui.PluginView;
import consulo.app.plugins.frontend.ui.PluginsAppLayout;
import consulo.hub.shared.repository.PluginNode;
import consulo.procoeton.core.vaadin.service.ProMainLayoutProvider;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Locale;

/**
 * @author VISTALL
 * @since 2025-05-10
 */
@Component
public class PluginsProMainLayoutProvider implements ProMainLayoutProvider {
    private final PluginsCacheService myPluginsCacheService;
    private final ObjectMapper myObjectMapper;
    private final SitemapCacheService mySitemapCacheService;
    private final TagsLocalizeLoader myTagsLocalizeLoader;

    public PluginsProMainLayoutProvider(PluginsCacheService pluginsCacheService,
                                        ObjectMapper objectMapper,
                                        SitemapCacheService sitemapCacheService,
                                        TagsLocalizeLoader tagsLocalizeLoader) {
        myPluginsCacheService = pluginsCacheService;
        myObjectMapper = objectMapper;
        mySitemapCacheService = sitemapCacheService;
        myTagsLocalizeLoader = tagsLocalizeLoader;
    }

    @Nonnull
    @Override
    public Class<? extends RouterLayout> getLayoutClass() {
        return PluginsAppLayout.class;
    }

    // hack for more faster meta search index
    @Override
    public IndexHtmlRequestListener createIndexHtmlRequestListener() {
        return indexHtmlResponse -> {
            VaadinRequest vaadinRequest = indexHtmlResponse.getVaadinRequest();

            if (isBot(vaadinRequest)) {
                String pathInfo = vaadinRequest.getPathInfo();

                if (pathInfo.startsWith("/v/")) {
                    String str = pathInfo.substring(3);

                    int slash = str.indexOf('/');
                    if (slash != -1) {
                        str = str.substring(0, slash);
                    }

                    String jsonLd =
                        myPluginsCacheService.getPluginsCache().getJsonLd(str, myObjectMapper, mySitemapCacheService, myTagsLocalizeLoader);

                    Element head = indexHtmlResponse.getDocument().head();
                    if (jsonLd != null) {
                        Element scriptTag = new Element("script");
                        scriptTag = scriptTag.attr("type", PluginsAppLayout.APPLICATION_LD_JSON);
                        scriptTag = scriptTag.text(jsonLd);

                        head.appendChild(scriptTag);
                    }

                    PluginNode node = myPluginsCacheService.getPluginsCache().mappped().get(str);
                    if (node != null) {
                        Element metaTag = new Element("meta");
                        metaTag = metaTag.attr("name", "description");
                        metaTag = metaTag.attr("content", PluginsCache.getDescription(node));

                        head.appendChild(metaTag);

                        if (node.iconBytes != null) {
                            Elements elements = head.getElementsByAttributeValue("rel", "icon");
                            for (Element element : elements) {
                                if ("link".equals(element.tagName())) {
                                    element.remove();
                                }
                            }

                            Element linkTag = new Element("link");
                            linkTag = linkTag.attr("href", PluginView.getImageUrl(node, false));
                            linkTag = linkTag.attr("rel", "icon");
                            linkTag = linkTag.attr("type", "image/svg+xml");

                            head.appendChild(linkTag);
                        }

                        Element canonicalLink = new Element("link");
                        canonicalLink = canonicalLink.attr("href", mySitemapCacheService.getPluginUrl(node));
                        canonicalLink = canonicalLink.attr("rel", "canonical");

                        head.appendChild(canonicalLink);
                    }
                }
            }
        };
    }

    private static boolean isBot(VaadinRequest request) {
        String ua = request.getHeader("User-Agent");
        if (ua == null) return false;
        ua = ua.toLowerCase(Locale.ROOT);

        return ua.contains("googlebot")
            || ua.contains("bingbot")
            || ua.contains("yandex")
            || ua.contains("duckduckbot")
            || ua.contains("baiduspider")
            || ua.contains("slurp");
    }
}
