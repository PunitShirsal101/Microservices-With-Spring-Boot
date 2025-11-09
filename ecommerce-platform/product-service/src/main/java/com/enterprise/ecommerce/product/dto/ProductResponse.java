package com.enterprise.ecommerce.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for product response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {
    
    private Long id;
    private String name;
    private String sku;
    private String description;
    private String detailedDescription;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private BigDecimal effectivePrice;
    private Integer stockQuantity;
    private Integer reservedQuantity;
    private Integer availableQuantity;
    private Boolean active;
    private Boolean featured;
    private Boolean inStock;
    private Boolean onSale;
    private String imageUrl;
    private List<String> additionalImages;
    private List<String> tags;
    
    // Product dimensions
    private BigDecimal weight;
    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal height;
    
    private String brand;
    private String manufacturer;
    private String model;
    
    // SEO fields
    private String metaTitle;
    private String metaDescription;
    private String metaKeywords;
    
    // Category information
    private Long categoryId;
    private String categoryName;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}