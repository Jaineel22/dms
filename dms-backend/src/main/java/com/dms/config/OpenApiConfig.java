package com.dms.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger UI configuration.
 *
 * <p>Registers the {@code bearerAuth} security scheme so every endpoint
 * annotated with {@code @SecurityRequirement(name = "bearerAuth")} gets
 * the "Authorize" padlock in Swagger UI.</p>
 *
 * <p>Swagger UI: <a href="http://localhost:8080/swagger-ui.html">http://localhost:8080/swagger-ui.html</a><br>
 * API Docs:    <a href="http://localhost:8080/v3/api-docs">http://localhost:8080/v3/api-docs</a></p>
 */
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title       = "DMS API",
        version     = "1.0.0",
        description = "Enterprise Document Management System — Phase 1 REST API",
        contact     = @Contact(
            name  = "DMS Support",
            email = "support@dms.com"
        ),
        license = @License(
            name = "Proprietary",
            url  = "https://dms.com/license"
        )
    ),
    servers = {
        @Server(url = "http://localhost:8080", description = "Local Development"),
        @Server(url = "https://api.dms.com",  description = "Production")
    }
)
@SecurityScheme(
    name         = "bearerAuth",
    type         = SecuritySchemeType.HTTP,
    scheme       = "bearer",
    bearerFormat = "JWT",
    description  = "Provide the JWT token obtained from POST /api/v1/auth/login. Format: Bearer <token>"
)
public class OpenApiConfig {
    // All configuration is via annotations — no bean methods required.
}