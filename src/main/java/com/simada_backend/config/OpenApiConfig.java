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
                        .title("SIMADA API")
                        .version("v1.0")
                        .description("Documentação da API do Sistema de Monitoramento de Atletas de Desempenho Avançado"));
    }
}
