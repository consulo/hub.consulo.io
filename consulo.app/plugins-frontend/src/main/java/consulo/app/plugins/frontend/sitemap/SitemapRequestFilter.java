package consulo.app.plugins.frontend.sitemap;

import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

/**
 * @author VISTALL
 * @since 2026-02-17
 */
@Configuration
public class SitemapRequestFilter {
    @Bean
    @ConditionalOnProperty(name = "vaadin.url-mapping")
    FilterRegistrationBean<?> sitemapFilter(@Value("${vaadin.url-mapping}") String urlMapping,
                                            @Nonnull SitemapCacheService sitemapCacheService) {
        String baseMapping = urlMapping.replaceFirst("/\\*$", "");
        FilterRegistrationBean<OncePerRequestFilter> registrationBean = new FilterRegistrationBean<>(
            new OncePerRequestFilter() {
                @Override
                protected void doFilterInternal(HttpServletRequest request,
                                                HttpServletResponse response,
                                                FilterChain filterChain)
                    throws ServletException, IOException {

                    SitemapCacheService.Sitemap sitemap = sitemapCacheService.get();

                    response.setHeader("Content-Type", "text/xml");
                    response.setHeader("Cache-Control", "private, max-age=0");
                    response.setHeader("Content-Length", String.valueOf(sitemap.data().length));
                    response.setHeader("Last-Modified", DateTimeFormatter.RFC_1123_DATE_TIME.format(sitemap.buildTime()));

                    ServletOutputStream stream = response.getOutputStream();
                    stream.write(sitemap.data());
                    stream.close();
                }
            });
        registrationBean.addUrlPatterns(baseMapping + "/sitemap.xml");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registrationBean;
    }
}
