package proiect_ispgl.proiect_ispgl.Config;

import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import proiect_ispgl.proiect_ispgl.Service.AccountSessionService;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

@Configuration
public class ApiSecurityConfiguration {
    @Bean
    public FilterRegistrationBean<CorsFilter> apiCorsFilter(@Value("${app.api.allowed-origins}") String origins) {
        var cors = new CorsConfiguration();
        cors.setAllowedOrigins(Arrays.stream(origins.split(",", -1)).map(String::strip)
                .map(ApiSecurityConfiguration::validatedOrigin).distinct().toList());
        cors.setAllowCredentials(true);
        cors.setAllowedMethods(List.of("GET", "HEAD", "POST", "DELETE"));
        cors.setAllowedHeaders(List.of(HttpHeaders.CONTENT_TYPE, AccountSessionService.CSRF_HEADER));
        cors.setExposedHeaders(List.of(AccountSessionService.CSRF_HEADER));

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", cors);
        var registration = new FilterRegistrationBean<>(new CorsFilter(source));
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.setDispatcherTypes(DispatcherType.REQUEST);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<ApiAccessFilter> apiAccessFilter(AccountSessionService sessions) {
        var registration = new FilterRegistrationBean<>(new ApiAccessFilter(sessions));
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        registration.setDispatcherTypes(DispatcherType.REQUEST);
        return registration;
    }

    private static String validatedOrigin(String value) {
        URI origin;
        try {
            origin = URI.create(value);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Each allowed origin must be an HTTP(S) origin.");
        }
        if (!("https".equals(origin.getScheme()) || "http".equals(origin.getScheme()))
                || origin.getHost() == null || origin.getRawUserInfo() != null || !"".equals(origin.getRawPath())
                || origin.getRawQuery() != null || origin.getRawFragment() != null
                || origin.getPort() == 0 || origin.getPort() > 65535 || origin.getRawAuthority().endsWith(":")) {
            throw new IllegalArgumentException("Each allowed origin must be an HTTP(S) origin without a path.");
        }
        return value;
    }
}
