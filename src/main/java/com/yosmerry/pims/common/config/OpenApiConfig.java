package com.yosmerry.pims.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  private static final String BEARER_AUTH = "bearerAuth";

  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("Personal Inventory Management System API")
            .description(
                "REST API for managing personal inventory and assets")
            .version("1.0.0")
            .contact(new Contact()
                .name("Yosmerry")
                .url("https://github.com/Yosmerry")))
        .components(new Components()
            .addSecuritySchemes(
                BEARER_AUTH,
                createBearerSecurityScheme()));
  }

  @Bean
  public OperationCustomizer commonHeaderCustomizer() {
    return (operation, handlerMethod) -> {
      operation.addParametersItem(createChannelIdParameter());
      operation.addParametersItem(createServiceIdParameter());
      operation.addParametersItem(createRequestIdParameter());

      return operation;
    };
  }

  private Parameter createChannelIdParameter() {
    return new Parameter()
        .in("header")
        .name("X-CHANNEL-ID")
        .description("Request channel identifier")
        .required(true)
        .example("WEB");
  }

  private Parameter createServiceIdParameter() {
    return new Parameter()
        .in("header")
        .name("X-SERVICE-ID")
        .description("Calling service identifier")
        .required(true)
        .example("PIMS-FE");
  }

  private Parameter createRequestIdParameter() {
    return new Parameter()
        .in("header")
        .name("X-REQUEST-ID")
        .description("Request identifier; generated when omitted")
        .required(false)
        .example("550e8400-e29b-41d4-a716-446655440000");
  }

  private SecurityScheme createBearerSecurityScheme() {
    return new SecurityScheme()
        .name(BEARER_AUTH)
        .type(SecurityScheme.Type.HTTP)
        .scheme("bearer")
        .bearerFormat("JWT");
  }
}