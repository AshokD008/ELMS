package com.lms.usermanagementservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableAsync
@EnableScheduling
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {

        configurer.setUseTrailingSlashMatch(true);
    }

    @Override
    public void configureContentNegotiation(
            ContentNegotiationConfigurer configurer
    ) {

        configurer
                .favorParameter(false)
                .ignoreAcceptHeader(false)
                .defaultContentType(
                        org.springframework.http.MediaType.APPLICATION_JSON
                );
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {

        // Custom converters or formatters can be added here
    }
}