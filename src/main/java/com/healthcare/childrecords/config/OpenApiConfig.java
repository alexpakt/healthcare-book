package com.healthcare.childrecords.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Electronic Child Health Records (ECHR) API")
                .description(
                    "RESTful API for managing child health records, growth measurements, vaccinations, "
                        + "clinical visits, and medical data. Designed for pediatricians, parents, and healthcare administrators.")
                .version("1.0.0")
                .contact(
                    new Contact()
                        .name("Alexandra Paktiti")
                        .email("al.paktiti@gmail.com"))
                .license(
                    new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT")))
        .addSecurityItem(new SecurityRequirement().addList("bearer"))
        .components(
            new io.swagger.v3.oas.models.Components()
                .addSecuritySchemes(
                    "bearer",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT Bearer token from /api/auth/login endpoint")));
  }
}
