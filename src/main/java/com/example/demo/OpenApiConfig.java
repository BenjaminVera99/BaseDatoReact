package com.example.demo;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition
public class OpenApiConfig {

    private static final String SCHEME_NAME = "bearerAuth";
    private static final String SCHEME = "Bearer";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Pasteleria Mil Sabores API")
                        .version("1.0")
                        .description(" Administracion para manejar C.R.U.D. de Productos al sistema")
                )
                .components(new Components()
                        .addSecuritySchemes(SCHEME_NAME, createSecurityScheme())
                )
                .addSecurityItem(new SecurityRequirement().addList(SCHEME_NAME));
    }

    private SecurityScheme createSecurityScheme() {
        return new SecurityScheme()
                .name(SCHEME_NAME)
                .type(SecurityScheme.Type.HTTP)
                .scheme(SCHEME)
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .description("Token JWT requerido para acceder a las rutas protegidas.");
    }
}