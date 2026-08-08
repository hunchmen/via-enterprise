package com.via.ems.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    /**
     * Configures the OpenAPI object for the Employee Management System API.
     * <p>
     * The OpenAPI object is used to generate the API documentation.
     * <p>
     * The API documentation is available at /v3/api-docs.
     * <p>
     * The API documentation is based on the OpenAPI 3.0.0 specification.
     */
    @Bean
    public OpenAPI emsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Employee Management System API")
                        .description("API documentation for Employee Management System")
                        .version("v1.0.0")
                        .contact(new Contact().name("EMS Support").email("support@via.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}
