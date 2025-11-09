package com.enterprise.ecommerce.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for product requests
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequest {
    
    @NotBlank(message = "Product name is required")
    @Size(max = 200, message = "Product name must not exceed 200 characters")
    private String name;
    
    @Size(max = 100, message = "SKU must not exceed 100 characters")
    private String sku;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    @Size(max = 2000, message = "Detailed description must not exceed 2000 characters")
    private String detailedDescription;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;
    
    @DecimalMin(value = "0.0", message = "Discount price must be greater than or equal to 0")
    private BigDecimal discountPrice;
    
    @Builder.Default
    private Integer stockQuantity = 0;
    
    @Builder.Default
    private Boolean active = true;
    
    @Builder.Default
    private Boolean featured = false;
    
    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    private String imageUrl;
    
    private List<String> additionalImages;
    
    private List<String> tags;
    
    // Product dimensions
    private BigDecimal weight;
    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal height;
    
    @Size(max = 50, message = "Brand must not exceed 50 characters")
    private String brand;
    
    @Size(max = 50, message = "Manufacturer must not exceed 50 characters")
    private String manufacturer;
    
    @Size(max = 100, message = "Model must not exceed 100 characters")
    private String model;
    
    // SEO fields
    @Size(max = 200, message = "Meta title must not exceed 200 characters")
    private String metaTitle;
    
    @Size(max = 500, message = "Meta description must not exceed 500 characters")
    private String metaDescription;
    
    @Size(max = 200, message = "Meta keywords must not exceed 200 characters")
    private String metaKeywords;
    
    @NotNull(message = "Category ID is required")
    private Long categoryId;
}