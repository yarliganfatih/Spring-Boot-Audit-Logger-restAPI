package com.draft.restapi.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Spring Boot Audit Logger REST API")
                        .version("1.0.0")
                        .description("REST API application with Audit Logging, Caching (Redis), Rate Limiting (Bucket4j), Resilience4j and OAuth2/JWT Security.")
                        .contact(new Contact().name("API Support").url("https://github.com/yarliganfatih/Spring-Boot-Audit-Logger-restAPI")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .addTagsItem(new Tag().name("Authentication / Token").description("OAuth2 access token endpoints."))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your OAuth2 / JWT bearer token to access secured API endpoints.")));
    }

    @Bean
    public OpenApiCustomiser oauthTokenEndpointCustomiser() {
        return openApi -> {
            Schema<?> tokenRequestSchema = new Schema<>().type("object")
                    .addProperty("grant_type", new Schema<>().type("string").example("password").description("Value: 'password' or 'refresh_token'"))
                    .addProperty("username", new Schema<>().type("string").example("user").description("Username (required for grant_type=password)"))
                    .addProperty("password", new Schema<>().type("string").example("user").description("Password (required for grant_type=password)"))
                    .addProperty("client_id", new Schema<>().type("string").example("mobile").description("OAuth2 Client ID"))
                    .addProperty("client_secret", new Schema<>().type("string").example("pin").description("OAuth2 Client Secret"))
                    .addProperty("refresh_token", new Schema<>().type("string").description("Refresh token (required for grant_type=refresh_token)"));

            Schema<?> tokenResponseSchema = new Schema<>().type("object")
                    .addProperty("access_token", new Schema<>().type("string").description("JWT Access Token"))
                    .addProperty("token_type", new Schema<>().type("string").example("bearer"))
                    .addProperty("refresh_token", new Schema<>().type("string").description("JWT Refresh Token"))
                    .addProperty("expires_in", new Schema<>().type("integer").description("Expiration time (in seconds)"))
                    .addProperty("scope", new Schema<>().type("string").description("Granted scope"));

            Operation tokenOperation = new Operation()
                    .addTagsItem("Authentication / Token")
                    .summary("Obtain / Refresh OAuth2 Access Token")
                    .description("OAuth2 token endpoint (automatically managed by Spring Security). Use username, password, and client credentials to obtain a JWT Access Token, or use a refresh_token to generate a new token.")
                    .requestBody(new RequestBody()
                            .content(new Content().addMediaType("application/x-www-form-urlencoded",
                                    new MediaType().schema(tokenRequestSchema))))
                    .responses(new ApiResponses()
                            .addApiResponse("200", new ApiResponse().description("Successful Token Response")
                                    .content(new Content().addMediaType("application/json",
                                            new MediaType().schema(tokenResponseSchema))))
                            .addApiResponse("400", new ApiResponse().description("Invalid Request / Bad Parameters"))
                            .addApiResponse("401", new ApiResponse().description("Unauthorized (Invalid Client / User Credentials)")));

            PathItem pathItem = new PathItem().post(tokenOperation);
            openApi.path("/oauth/token", pathItem);
        };
    }
}
