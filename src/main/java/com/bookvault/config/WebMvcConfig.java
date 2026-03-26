package com.bookvault.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

/**
 * WebMvcConfig
 *
 * Configures:
 *  - Static resource handlers and cache headers
 *  - CORS (mirrors SecurityConfig — kept in sync)
 *  - View controller shortcuts for simple redirect pages
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.security.allowed-origins}")
    private String allowedOrigins;

    // ── Static Resources ───────────────────────────────────────

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve uploaded PDFs under /uploads/** only for authenticated users
        // (access control is enforced by BookController /api/v1/books/{id}/pdf)
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./uploads/")
                .setCachePeriod(0); // no browser caching for uploaded content

        // Standard static assets
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/")
                .setCachePeriod(3600);

        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/")
                .setCachePeriod(3600);

        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/")
                .setCachePeriod(86400);
    }

    // ── View Controllers ───────────────────────────────────────

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Simple redirect: /home → /
        registry.addRedirectViewController("/home", "/");
        // Explicit mapping for /books/public (synonymous with /books)
        registry.addRedirectViewController("/books/public", "/books");
    }

    // ── CORS ───────────────────────────────────────────────────
    // Note: The primary CORS config is in SecurityConfig.corsConfigurationSource().
    // This addCorsMappings() applies to non-security MVC paths (e.g., error handlers).

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        for (String origin : allowedOrigins.split(",")) {
            registry.addMapping("/api/**")
                    .allowedOrigins(origin.trim())
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(true)
                    .maxAge(3600);
        }
    }
}
