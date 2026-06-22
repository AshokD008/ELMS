package com.lms.usermanagementservice.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class SwaggerConfig {

    public OpenAPI openAPI() {

        return new OpenAPI()
                .info(apiInfo())
                .externalDocs(apiDocs());
    }

    private Info apiInfo() {

        return new Info()
                .title("LMS User Management Service API")
                .description("Production Grade User Management APIs for LMS  Platform")
                .version("1.0.0")
                .contact(
                        new Contact()
                                .name("LMS  Team")
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
                .description("LMS  Documentation")
                .url("https://docs.lms.com");
    }
}