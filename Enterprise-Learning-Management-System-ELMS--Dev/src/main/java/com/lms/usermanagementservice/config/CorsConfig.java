package com.lms.usermanagementservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 1. Explicitly allow your Vite React development port (No duplicate overrides)
        configuration.setAllowedOrigins(List.of("http://localhost:5174"));

        // 2. HTTP methods mapping
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // 3. FIX: CRUCIAL FOR PREFLIGHT REQUESTS - Allow headers incoming from Axios
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-Requested-With"));

        // 4. Mandate credential passing (Required for local development sessions/cookies/bearers)
        configuration.setAllowCredentials(true);

        // 5. Expose specific headers to the frontend Axios environment
        configuration.setExposedHeaders(List.of("Authorization", "Content-Type"));

        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}