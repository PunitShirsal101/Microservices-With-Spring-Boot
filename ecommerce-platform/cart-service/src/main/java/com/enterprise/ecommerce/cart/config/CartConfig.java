package com.enterprise.ecommerce.cart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration class for Cart Service
 */
@Configuration
public class CartConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}