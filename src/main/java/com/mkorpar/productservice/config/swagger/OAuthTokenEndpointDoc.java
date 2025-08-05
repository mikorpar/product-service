package com.mkorpar.productservice.config.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Operation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.mkorpar.productservice.constants.SwaggerConstants.*;

@Configuration
@Profile("!noauth & !external-auth-server")
public class OAuthTokenEndpointDoc {

    @Bean
    public OpenAPI customOpenAPIConfig() {
        Operation postOperation = new Operation()
                .operationId("getToken")
                .summary("Token endpoint")
                .description("""
                Obtain OAuth2 access token using client_credentials grant type and \
                client_secret_post authentication method."""
                ).requestBody(getRequestBody())
                .responses(getResponses());

        return new OpenAPI()
                .info(new Info().title("Authorization Server API").version("1.0"))
                .components(new Components())
                .path(TOKEN_ENDPOINT, new PathItem().post(postOperation));
    }

    private RequestBody getRequestBody() {
        Map<String, Schema> properties = new LinkedHashMap<>();
        properties.put(GRANT_TYPE, new StringSchema()
                ._default("client_credentials")
                .example("client_credentials")
                .description("OAuth2 grant type"));
        properties.put(CLIENT_ID, new StringSchema()
                .example("test-client")
                .description("Client ID"));
        properties.put(CLIENT_SECRET, new StringSchema()
                .example("test-secret")
                .description("Client Secret"));

        Schema<?> formSchema = new Schema<>().type("object");
        formSchema.setProperties(properties);

        return new RequestBody()
                .required(true)
                .content(new Content().addMediaType(APPLICATION_FORM_URLENCODED_VALUE,
                        new MediaType().schema(formSchema)));
    }

    private ApiResponses getResponses() {
        Map<String, Schema> tokenProperties = new LinkedHashMap<>();
        tokenProperties.put(ACCESS_TOKEN, new StringSchema()
                .description("JWT access token")
                .example(getAccessToken()));
        tokenProperties.put(TOKEN_TYPE, new StringSchema()
                .description("Type of token issued")
                .example("Bearer"));
        tokenProperties.put(EXPIRES_IN, new IntegerSchema()
                .description("Lifetime in seconds")
                .example(300));

        Schema<?> tokenResponseSchema = new Schema<>().type("object");
        tokenResponseSchema.setProperties(tokenProperties);

        return new ApiResponses()
                .addApiResponse(OK, new ApiResponse()
                        .description("Successful token response")
                        .content(new Content().addMediaType(APPLICATION_JSON,
                                new MediaType().schema(tokenResponseSchema))))
                .addApiResponse(BAD_REQUEST, new ApiResponse().description("Invalid request"))
                .addApiResponse(UNAUTHORIZED, new ApiResponse().description("Invalid client credentials"));
    }

    private String getAccessToken() {
        return """
    eyJraWQiOiJqdHctc2lnbmF0dXJlLXJzYS1rZXkiLCJhbGciOiJSUzI1NiJ9.\
    eyJzdWIiOiJ0ZXN0LWNsaWVudCIsImF1ZCI6InRlc3QtY2xpZW50IiwibmJmIjoxNzU0NDI0MTEzLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODA\
    iLCJleHAiOjE3NTQ0MjQ0MTMsImlhdCI6MTc1NDQyNDExMywianRpIjoiYWIzMTZjYTktZTk3OS00YTk1LWI0ZGYtYjE4MTc1YTg3NGQxIn0.\
    O_cX3S5dQhn1SuJAuATu9H4ktbjNK6TqZc95TY1UFVGLd3ZrK4dtcS2SjnkOEMdw7AgvyRn_gmYWFjA2BcIb51Jm171BBmAzbw1g4VQNkBpwoNNb1ut\
    aKAlbKr3aIrGK10H8GwQU6VGIvbFlXdKp3TE3pWQe1O7zGnGP4BkUPDoZz8GfoE4ZBKCwNaCgtOxjeoAHOG6kR1UeTuAyeqLWxMnrgKzskCXiLG41Gx\
    6igsxrwrrIAAAGP5AycJHpa3-NM1CfXUuDlmDG6n1M8mMLqNZwg8pFUAVieXb0hBVq3japjTQz70OQeSBlF4o1dJAXqSGJCTbSddpfdOepvLvJMg
    """;
    }

}
