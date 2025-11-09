package com.enterprise.ecommerce.cart.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

/**
 * Client for communicating with Product Service
 */
@Component
public class ProductClient {

    private static final Logger logger = LoggerFactory.getLogger(ProductClient.class);

    @Value("${app.services.product-service.url:http://localhost:8082}")
    private String productServiceUrl;

    @Value("${app.services.product-service.api-path:/api/products/}")
    private String productsApiPath;

    private final RestTemplate restTemplate;

    public ProductClient() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Get product by ID from Product Service
     */
    public ProductResponse getProductById(Long productId) {
        try {
            String url = productServiceUrl + productsApiPath + productId;
            logger.info("Fetching product from: {}", url);
            
            return restTemplate.getForObject(url, ProductResponse.class);
        } catch (RestClientException e) {
            String errorMessage = String.format("Failed to fetch product details for ID: %d from product service at URL: %s. Cause: %s", 
                                               productId, productServiceUrl + productsApiPath + productId, e.getMessage());
            logger.error(errorMessage, e);
            // Handle the RestClientException by logging and returning null or default response
            logger.warn("Product service unavailable, returning null for product ID: {}", productId);
            return null;
        } catch (Exception e) {
            String errorMessage = String.format("Unexpected error while fetching product details for ID: %d from product service at URL: %s. Error type: %s", 
                                               productId, productServiceUrl + productsApiPath + productId, e.getClass().getSimpleName());
            logger.error(errorMessage, e);
            
            // Handle different types of unexpected exceptions
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Product fetch operation was interrupted", e);
            } else if (e instanceof SecurityException) {
                throw new IllegalStateException("Security error while accessing product service", e);
            } else {
                // For all other unexpected exceptions, wrap with additional context
                String enhancedMessage = errorMessage + ". Original cause: " + (e.getMessage() != null ? e.getMessage() : "Unknown error");
                throw new IllegalStateException(enhancedMessage, e);
            }
        }
    }

    /**
     * Product response DTO
     */
    public static class ProductResponse {
        private Long id;
        private String name;
        private String description;
        private BigDecimal price;
        private String imageUrl;
        private String category;
        private Integer stockQuantity;
        private boolean active;

        // Constructors
        public ProductResponse() {
            // Default constructor for JSON deserialization
        }

        // Getters and Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public Integer getStockQuantity() {
            return stockQuantity;
        }

        public void setStockQuantity(Integer stockQuantity) {
            this.stockQuantity = stockQuantity;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }
    }
}