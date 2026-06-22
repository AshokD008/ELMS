package com.lms.usermanagementservice.config;

import jakarta.servlet.Filter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@Configuration
@Slf4j
public class LoggerConfig {

    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {

        CommonsRequestLoggingFilter loggingFilter =
                new CommonsRequestLoggingFilter();

        loggingFilter.setIncludeClientInfo(true);
        loggingFilter.setIncludeQueryString(true);
        loggingFilter.setIncludePayload(true);
        loggingFilter.setIncludeHeaders(false);
        loggingFilter.setMaxPayloadLength(10000);

        return loggingFilter;
    }

    @Bean
    public FilterRegistrationBean<Filter> loggingFilterRegistration(
            CommonsRequestLoggingFilter filter
    ) {

        FilterRegistrationBean<Filter> registrationBean =
                new FilterRegistrationBean<>();

        registrationBean.setFilter(filter);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(1);

        log.info("Request logging filter configured successfully");

        return registrationBean;
    }
}