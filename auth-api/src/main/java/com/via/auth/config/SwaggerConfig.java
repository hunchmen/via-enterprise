package com.via.auth.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    /**
     * Configures the OpenAPI object for the Authentication Management System API.
     * <p>
     * The OpenAPI object is used to generate the API documentation.
     * <p>
     * The API documentation is available at /v3/api-docs.
     * <p>
     * The API documentation is based on the OpenAPI 3.0.0 specification.
     */
    @Bean
    public OpenAPI authOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes(
                                "sessionCookie",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.COOKIE)
                                        .name("JSESSIONID")
                                        .description("Session cookie returned after a successful login")))
                .info(new Info()
                        .title("Authentication Management System API")
                        .description("API documentation for Authentication Management System")
                        .version("v1.0.0")
                        .contact(new Contact().name("VIA Enterprise Support").email("support@via.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}
