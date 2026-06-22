package com.lms.usermanagementservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {

        return new OpenAPI()
                .info(apiInfo())
                .externalDocs(apiDocs())
                .addSecurityItem(
                        new SecurityRequirement()
                                .addList(SECURITY_SCHEME_NAME)
                )
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        SECURITY_SCHEME_NAME,
                                        securityScheme()
                                )
                );
    }

    private Info apiInfo() {

        return new Info()
                .title("LMS User Management Service API")
                .description("Production Grade User Management APIs for LMS Platform")
                .version("1.0.0")
                .contact(
                        new Contact()
                                .name("LMS Team")
                                .email("support@lms.com")
                                .url("https://lms.com")
                )
                .license(
                        new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")
                );
    }

    private ExternalDocumentation apiDocs() {

        return new ExternalDocumentation()
                .description("LMS Documentation")
                .url("https://docs.lms.com");
    }

    private SecurityScheme securityScheme() {

        return new SecurityScheme()
                .name("Authorization")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER);
    }
}