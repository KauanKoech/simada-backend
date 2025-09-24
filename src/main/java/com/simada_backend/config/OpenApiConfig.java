package com.simada_backend.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI simadaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("WIKO API")
                        .version("v1.0")
                        .description("\n" +
                                "Intelligent System for Monitoring Training Load, Performance and Injury Risk in Athletes API Documentation"));
    }
}
