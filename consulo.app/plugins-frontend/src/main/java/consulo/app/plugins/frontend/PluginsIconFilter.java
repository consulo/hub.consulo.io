package consulo.app.plugins.frontend;

import consulo.app.plugins.frontend.backend.PluginsCache;
import consulo.app.plugins.frontend.backend.PluginsCacheService;
import consulo.hub.shared.repository.PluginNode;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

/**
 * @author VISTALL
 * @since 2025-05-12
 */
@Configuration
public class PluginsIconFilter {
    private final byte[] myPluginIcon;

    public PluginsIconFilter() {
        InputStream resource = getClass().getResourceAsStream("/images/pluginBig.svg");
        try {
            myPluginIcon = IOUtils.toByteArray(resource);
        }
        catch (IOException e) {
            throw new Error(e);
        }
    }

    @Bean
    @ConditionalOnProperty(name = "vaadin.url-mapping")
    FilterRegistrationBean<?> iconFilter(@Value("${vaadin.url-mapping}") String urlMapping,
                                         @Nonnull PluginsCacheService pluginsCacheService) {
        String baseMapping = urlMapping.replaceFirst("/\\*$", "");
        FilterRegistrationBean<OncePerRequestFilter> registrationBean = new FilterRegistrationBean<>(
            new OncePerRequestFilter() {
                @Override
                protected void doFilterInternal(HttpServletRequest request,
                                                HttpServletResponse response,
                                                FilterChain filterChain)
                    throws ServletException, IOException {
                    String path = request.getRequestURI().substring(baseMapping.length());

                    boolean isDark = Boolean.parseBoolean(request.getParameter("dark"));

                    String pluginId = StringUtils.removeStart(path, "/i/");

                    byte[] data = null;
                    if ("consulo.plugin".equals(pluginId)) {
                        data = myPluginIcon;
                    }
                    else {
                        PluginsCache pluginsCache = pluginsCacheService.getPluginsCache();
                        PluginNode pluginNode = pluginsCache.mappped().get(pluginId);
                        if (pluginNode != null) {
                            String iconBytes = isDark ? pluginNode.iconDarkBytes : pluginNode.iconBytes;
                            if (iconBytes == null) {
                                iconBytes = pluginNode.iconBytes;
                            }

                            if (iconBytes != null) {
                                data = Base64.getDecoder().decode(iconBytes);
                            }
                        }
                    }

                    if (data == null) {
                        data = myPluginIcon;
                    }

                    response.setHeader("Content-Type", "image/svg+xml");
                    response.setHeader("Cache-Control", "private, max-age=0");

                    ServletOutputStream stream = response.getOutputStream();
                    stream.write(data);
                    stream.close();
                }
            });
        registrationBean.addUrlPatterns(baseMapping + "/i/*");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registrationBean;
    }
}
