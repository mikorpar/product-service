package com.mkorpar.productservice.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;

@Configuration
@EnableCaching
public class ProductServiceConfiguration {

    @Bean
    public RestClient restClient(RestClient.Builder restClientBuilder) {
        return restClientBuilder
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {})
                .build();
    }

}
