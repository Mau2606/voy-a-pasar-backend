package com.manualjudicial.auth;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

/**
 * Configures HTTP-level caching optimizations:
 * - ShallowEtagHeaderFilter: Automatically generates ETag headers for
 *   all API responses. When the client sends If-None-Match, the server
 *   returns 304 Not Modified if content hasn't changed, saving bandwidth.
 */
@Configuration
public class WebCacheConfig {

    @Bean
    public FilterRegistrationBean<ShallowEtagHeaderFilter> etagFilter() {
        FilterRegistrationBean<ShallowEtagHeaderFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new ShallowEtagHeaderFilter());
        registration.addUrlPatterns("/api/*");
        registration.setName("etagFilter");
        registration.setOrder(1);
        return registration;
    }
}
