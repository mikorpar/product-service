package com.mkorpar.productservice.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableCaching
public class ProductServiceConfiguration {

    @Bean
    public RestClient restClient() {
        return RestClient.create();
    }

}
