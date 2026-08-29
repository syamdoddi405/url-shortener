package com.url.shortener.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI urlShortenerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("URL Shortener API")
                        .description("""
                                Production-oriented URL Shortener REST API.

                                Features:
                                - URL shortening
                                - URL expansion
                                - URL statistics
                                - Click analytics
                                - Redis caching
                                - Kafka-based analytics events
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Syam Doddi"))
                        .license(new License()
                                .name("Apache 2.0")));
    }
}